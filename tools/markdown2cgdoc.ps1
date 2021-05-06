param([Parameter(Mandatory = $true)][String]$Source, [String]$ReleaseDestination = "", [String]$ReviewDestination = "", $Leagues = 0, $Language = 'en')
#Requires -Modules Microsoft.PowerShell.Utility

#################################################################################
# CONFIGURATION                                                                 #
#################################################################################

$NEW_TOKEN ="🟢"

$REPLACE_REGEX = @(
    @{ regex = '([^`])`([a-z]{1}[^`\s]*)`'; replace = '$1<var>$2</var>' },
    @{ regex = '([^`])`([A-Z]{1}[^`\s]*)`'; replace = '$1<action>$2</action>' },
    @{ regex = '([^`])`((-|\+)?[0-9∞]{1}[^`\s]*)\s*`'; replace = '$1<const>$2</const>' }
)

$HEADER3_CSS = 'font-size:14px;font-weight:700;padding-top:15px;color:#838891;padding-bottom:15px';

$INSERT_REGEX = @(
    @{ regex = '^\|.+\|$'; insert = '{.marked}'; label = 'table' }
    @{ regex = '^###\s'; insert = "{style=`"$HEADER3_CSS`"}"; label = 'h3' }
)

$FILTER_REGEX = '^(([^:]+)\s+)?([A-Z]+)\s*(>=|<=|==)\s*(\d)s*$';
$HEADER_REGEX = '^(#+)\s*(.*)\s*$'
$LINE_SPAN_REGEX = '^([^\:]*)(\:(.*))?$'
$MATCH_CONDITIONAL_STATEMENT_REGEX = '\s*[A-Z]+\s*[><=]+\s*[0-9]+\s*$'

#################################################################################
$DEFAULT_PLAIN = { param ($node, $layout, $title, $content)
    "
<div class='no-cg-handled'>
<h$($node.level)>$title</h$($node.level)>

$content

</div>
"
}

$DEFAULT_H2 = { param ($node, $layout, $title, $content)
    $name = $layout.name;
    "
<!-- $name -->
<div class='statement-section statement-$name'>
<h2 style='padding-top:15px'><span>$title</span>
</h2>
<div class='statement-$name-content'>

$content

</div>
</div>";
}

#################################################################################
$SECTION_CG_H2 = { param ($node, $layout, $title, $content)
    $name = $layout.name;
    "
<!-- $name -->
<div class='statement-section statement-$name'>
<h2><span class='icon icon-$name'>&nbsp;</span><span>$title</span></h2>
<div class='statement-$name-content'>
$content
</div>
</div>";
}

#################################################################################
$CONDITION_BLOCK_CG_H3 = { param ($node, $layout, $title, $content)
    $name = $layout.name;
    "<div class='statement-$name-conditions'>
    <div class='icon $name'></div><div class='blk'>
        <div class='title'>$title</div>
        <div class='text'>
$content
</div>
    </div>
</div>"
}

$PROTOCOL_BLOCK = { param ($node, $layout, $title, $content)
    "<div class='blk'>
<div class='title'>$title</div>
<div class='text'>
$content
</div>
</div>
"
}

$NO_LINE_SPAN = { param ($node, $layout, $title, $content)
    $parsed = $title | Select-String -Pattern $LINE_SPAN_REGEX;
    $span = $parsed.Matches.Groups[1];
    $desc = $parsed.Matches.Groups[3];
    "<span class='statement-lineno'>$span</span>$desc

$content

"
}

#################################################################################
$LAYOUT = @(
    @{ token = '🎯'; name = 'goal'; label = 'The Goal'; render = $SECTION_CG_H2 },
    @{ token = '🐯'; name = 'expertrules'; label = 'Expert Rules'; render = $SECTION_CG_H2 },
    @{ token = '⚠️'; name = 'warning'; label = 'Note'; render = $SECTION_CG_H2 },
    @{ token = '🧾'; name = 'protocol'; label = 'Game Protocol'; render = $SECTION_CG_H2
        accepts = @(
            @{ token = '👀'; name = 'input'; label = 'Input'; render = $PROTOCOL_BLOCK;
                accepts = @( @{token = '📑'; label = 'Line'; render = $NO_LINE_SPAN })
            },
            @{ token = '💬'; name = 'output'; label = 'Output'; render = $PROTOCOL_BLOCK;
                accepts = @( @{token = '📑'; label = 'Line' ; render = $NO_LINE_SPAN })
            }
            @{ token = '⚓'; name = 'contrainst'; label = 'Constraints'; render = $PROTOCOL_BLOCK; accepts = @() })
    },
    @{ token = '📝'; name = 'pseudocode'; label = 'Pseudocode'; render = $SECTION_CG_H2 },
    @{ token = '💡'; name = 'hint'; label = 'Hint'; render = $SECTION_CG_H2 },
    @{ token = '✔️'; name = 'rules'; label = 'Rules'; render = $SECTION_CG_H2
        accepts = @(
            @{ token = '🏆'; name = 'victory'; label = 'Victory Conditions'; render = $CONDITION_BLOCK_CG_H3 },
            @{ token = '☠️'; name = 'lose'; label = 'Defeat Conditions'; render = $CONDITION_BLOCK_CG_H3 }
        )
    }
    @{ token = '🎯'; name = 'goal'; label = 'The Goal'; render = $SECTION_CG_H2 }
    @{ token = ''; name = 'default'; label = 'Custom'; render = $DEFAULT_H2 }
)

#################################################################################
############################### parsing methods     #############################
#################################################################################

function getParent($child, $level) {
    $ret = $child;
    while (($null -ne $ret.parent) -and ($ret.level -ge $level)) {
        if ($ret -eq $ret.parent) {
            Write-Error "infinite loop: submit the issue"; exit 1;
        }
        $ret = $ret.parent;
    }
    return $ret;
}

function parseFilter ($node, $parameters) {
    $parsed = $node.title | Select-String -Pattern $FILTER_REGEX -CaseSensitive
    if ($parsed.Matches.Success) {
        $label = $parsed.Matches.Groups[2].Value;
        $reference = $parsed.Matches.Groups[3].Value;
        $operator = $parsed.Matches.Groups[4].Value;
        $value = [int]$parsed.Matches.Groups[5].Value;

        if ($parameters.ContainsKey($reference)) {
            $node.filter = @{
                reference = $reference;
                value     = $value;
                operator  = $operator;
                label     = $label
            }
        }
        else {
            $warn = $parameters.Keys -join '|';
            Write-Warning ""
            Write-Warning "Excepted $($warn): found '$reference'"
            Write-Warning "  => the '$('#'*$node.level) $($node.title)' statement is ignored"
            Write-Warning "  fixit: remove the statement or inject '$reference'";
            Write-Warning ""
        }
    }
}
function parseSummary($md, $parameters) {
    $document = @{children = @(); parent = $null; content = $null; title = "__ROOT__"; level = 0; filter = $null }
    $parent = $document
    $current = $document
    $p = "";
    $md -split "`r`n" | ForEach-Object {
        $g = $_ | Select-String -Pattern $HEADER_REGEX
        if ($g.Matches.Success) {

            $title = $g.Matches.Groups[2].Value;
            $level = $g.Matches.Groups[1].Value.Length;
            $current.content = $p;
            $p = "";
            $parent = getParent -child $current -level ($level);
            $current = @{children = @(); parent = $parent; content = $null; title = $title; level = $level; filter = $null };
            parseFilter -node $current -parameters $parameters
            $parent.children += @($current);
        }
        else {
            $p += "$_`n";
        }
    }
    $current.content = $p;
    return $document;
}
function debugSummary($node, $padding) {
    Write-Output "$($padding) $($node.level) $($node.title)";
    $node.children | % { debugSummary -node $_ -padding "$($padding)-" }
}
function getOuterMd($node) {
    Write-Output "$('#'*$node.level) $($node.title)";
    Write-Output $node.content;
    $node.children | % { Write-Output (getOuterMd -node $_) }
}
function getInnerMd($node) {
    Write-Output $node.content;
    $node.children | % { Write-Output (getOuterMd -node $_) }
}
function getGate($node, $parameters) {
    $accepted = $true;
    $new = $false;
    if ($null -ne $node.filter) {
        $const = $parameters[$node.filter.reference];
        if ($node.filter.operator -eq ">=") {
            $accepted = $const -ge $node.filter.value;
            $new = $const -eq $node.filter.value;
        }
        elseif ($node.filter.operator -eq "<=") {
            $accepted = $const -le $node.filter.value;
        }
        elseif ($node.filter.operator -eq "==") {
            $accepted = $const -eq $node.filter.value;
        }
        Write-Debug "evaluate $($node.filter.reference):$const $($node.filter.operator) $($node.filter.value)";
    }
    return @{ open = $accepted; new = $new }
}

#################################################################################
############################# Templating   ######################################
#################################################################################

function pimpMd($content) {
    $REPLACE_REGEX | % {
        $content = $content -creplace $_.regex, $_.replace
    }
    $empty = $true;
    $content -split "`n" | % {
        if ($empty) {
            foreach ($rule in $INSERT_REGEX) {
                if ($_ -match $rule.regex) {
                    Write-Debug "the insert rule '$($rule.label)' matched the line '$_'";
                    write-output $rule.insert;
                }
            }
        }
        write-output $_;
        $empty = [String]::IsNullOrWhiteSpace($_);
    }
}

function templateHtml($content) {
    return "
<html>
<head>
<style>
.statement-icon_statement_download{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:0 -25px;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_examples{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-20px -25px;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_expert_rules{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-50px 0;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_goal{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-50px -20px;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_hint{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:0 -45px;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_lose_conditions{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:0 0;width:25px;height:25px;background-repeat:no-repeat}.statement-icon_statement_protocol{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-20px -45px;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_pseudo_code_algorithm{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-40px -45px;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_rules{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-70px 0;width:20px;height:20px;background-repeat:no-repeat}.statement-icon_statement_victory_conditions{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-25px 0;width:25px;height:25px;background-repeat:no-repeat}.statement-icon_statement_warning{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-70px -20px;width:20px;height:20px;background-repeat:no-repeat}.marked{padding:15px}.marked h1{font-size:2em;margin:.67em 0}.marked h2{font-size:1.5em;margin:.75em 0}.marked h3{font-size:1.2em;margin:.83em 0}.marked h4{margin:1.12em 0}.marked blockquote{padding:10px 20px;margin:0 0 20px;border-left:5px solid #e7e9eb}.marked blockquote p{color:#838891;margin-bottom:0}.marked a,.marked a:active,.marked a:hover,.marked a:visited{transition:color .2s ease-in-out;border-bottom:none}.marked a,.marked a:active,.marked a:visited{color:#1a99aa}.marked a:hover{color:rgba(26,153,170,.8)}.marked table{border-collapse:collapse;background:#f9f9f9;width:100%}.marked table,.marked td,.marked th{border:1px solid #dadada}.marked td,.marked th{padding:5px}.marked pre{margin-bottom:20px}.marked img{display:block;margin:0 auto 20px}pre{font-family:monospace;padding:0;margin:0;font-size:inherit;color:inherit;word-break:inherit;word-wrap:inherit;background-color:inherit;border:none}.statement_wrapping_div{margin:20px}.statement_content{z-index:1;position:relative;overflow-y:scroll;height:100%;padding-bottom:20px}.statement_content.retroStatement{overflow-y:initial}ol,ul{padding-left:20px;margin-top:10px;margin-bottom:10px}ol li{list-style-type:inherit}li{margin-bottom:4px;list-style-type:disc}p,pre{margin-bottom:10px}a,a:active,a:hover,a:visited{color:#454c55;border-bottom:1px dotted #454c55}#statement_back{position:absolute;top:0;left:0;width:100%;height:100%;padding-top:56.25%;z-index:0;background-size:cover;background-position:50% 0;background-repeat:no-repeat}.statement_back{display:none}action{display:inline-block;font-family:Inconsolata,consolas,monospace;padding:0 4px;background-color:#18a1ea;white-space:nowrap;margin:0;color:#fff;font-weight:400;border-radius:3px}.explanation var{background-color:rgba(0,0,0,.85);padding:0 2px}.story_box{position:relative;padding:15px 40px;background-color:rgba(0,0,0,.8);min-height:60px}.story_box .story_opening{display:inline-block;font-weight:700;font-size:16px;color:#fff;line-height:30px}.story_box .story{color:#fff;display:none;margin-top:10px}.view_more:hover{background:#fff}.view_more{transition-duration:.2s;text-transform:uppercase;text-align:left;background:#f2bb13;padding:10px 20px;float:right;color:#000;font-size:12px;line-height:12px;min-width:120px}.view_more .symbol{float:right;font-size:14px}.disclaimer{padding:30px 20px 30px 200px;min-height:120px;margin-top:10px;background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGoAAAB4CAYAAAAaP2cHAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyFpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNS1jMDIxIDc5LjE1NDkxMSwgMjAxMy8xMC8yOS0xMTo0NzoxNiAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDpDMzc5NjI3MTc2M0ExMUU0QkU2MEIwMTdEMkQ3OUNDQSIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDpDMzc5NjI3Mjc2M0ExMUU0QkU2MEIwMTdEMkQ3OUNDQSI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOkMzNzk2MjZGNzYzQTExRTRCRTYwQjAxN0QyRDc5Q0NBIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOkMzNzk2MjcwNzYzQTExRTRCRTYwQjAxN0QyRDc5Q0NBIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+J5JrwwAACURJREFUeNrsnWtsVEUU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);background-repeat:no-repeat;background-position:47px}.disclaimer,.explanation{background-color:hsla(0,0%,100%,.85);margin-left:20px;margin-right:20px}.explanation{padding:20px;margin-top:20px}.protocol{background-color:rgba(0,0,0,.85);padding:20px;color:#fff;margin-top:10px;margin-left:20px;margin-right:20px}.protocol .protocol_title{text-transform:uppercase;display:block;font-weight:700;color:#f2bb13;margin-top:20px}.protocol .protocol_title:first-of-type{margin-top:0}.protocol .protocol_line{margin-top:5px}.protocol .initNText,.protocol .loopNText{display:inline}.part_title{display:block;font-weight:700;margin-bottom:5px}.example{margin-left:20px;margin-top:10px;margin-right:20px;position:relative;padding-top:20px;padding-bottom:20px;display:block;background-color:hsla(0,0%,100%,.85)}.example .example_presentation_container{display:block;padding-left:20px;padding-right:20px;overflow-x:auto}.example .example_presentation_container .example_presentation{background-color:#fff;display:table;width:100%}.example .example_presentation_container .example_presentation .illustration_wrapper{display:table-cell;vertical-align:middle}.example .example_presentation_container .example_presentation .illustration{width:100%;display:block}.example .example_presentation_container .example_presentation .text{background:#454c55;padding:20px;text-align:center;display:table-cell;vertical-align:middle;text-transform:uppercase;color:#fff}.example .example_rounds{margin-left:20px;display:-webkit-box;display:-moz-box;display:-ms-flexbox;display:-webkit-flex;display:flex;height:100%;flex-direction:row;-webkit-flex-direction:row;-webkit-align-content:stretch;align-content:space-between;align-items:stretch;flex-wrap:wrap;justify-content:space-between}.example .example_rounds .round_container{margin-top:20px;flex:1;-webkit-flex:1;-ms-flex:1;-webkit-box-flex:1;-moz-box-flex:1;display:block;text-align:center;margin-right:20px;background-color:#454c55}.example .example_rounds .round{width:100%;height:100%;display:inline-block;text-align:left;background-color:#454c55;text-transform:uppercase}.example .example_rounds .round .illustration{width:100%}.example .example_rounds .round .text{word-wrap:break-word;padding:15px;color:#fff}.example .example_rounds .round .round_title{text-transform:uppercase;display:block;font-weight:700;color:#f2bb13}.example .example_conclusion_container{display:block;padding-left:20px;padding-right:20px;margin-top:10px}.example .example_conclusion_container .example_conclusion{background-color:hsla(0,0%,100%,.9);min-height:110px;display:table;width:100%}.example .example_conclusion_container .example_conclusion .text{text-transform:uppercase;display:table-cell;vertical-align:middle;text-align:center}.example_old{padding:10px 20px;background-color:hsla(0,0%,100%,.85);margin-left:20px;margin-right:20px;margin-top:10px}.statement-container{position:absolute;top:0;left:0;padding-top:300px;width:100%;height:100%;overflow:auto;scrollbar-color:rgba(0,0,0,.2) transparent;scrollbar-width:thin}.statement-container::-webkit-scrollbar{width:14px;height:14px;background-color:#fff}.statement-container::-webkit-scrollbar-thumb{min-height:40px;border:4px solid transparent;background-clip:padding-box;-webkit-border-radius:7px;background-color:rgba(0,0,0,.2)}.statement-container::-webkit-scrollbar-corner{background:0 0}.statement-cover{background-size:cover;min-height:300px}.question-statement-example-in,.question-statement-example-out{white-space:pre}.statement-body{color:#454c55;background-color:#fff;padding-top:15px;font-weight:400;font-size:14px;line-height:16px}.statement-body h1,.statement-body h2{color:#838891;font-size:14px;font-weight:700;margin-bottom:15px;margin-top:0}.statement-body h1 .icon,.statement-body h2 .icon{margin-left:2px}.statement-body h1 .icon.icon-goal,.statement-body h2 .icon.icon-goal{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-50px -20px;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-rules,.statement-body h2 .icon.icon-rules{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-70px 0;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-expertrules,.statement-body h2 .icon.icon-expertrules{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-50px 0;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-pseudocode,.statement-body h2 .icon.icon-pseudocode{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-40px -45px;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-warning,.statement-body h2 .icon.icon-warning{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-70px -20px;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-protocol,.statement-body h2 .icon.icon-protocol{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-20px -45px;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-example,.statement-body h2 .icon.icon-example{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-20px -25px;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 .icon.icon-hint,.statement-body h2 .icon.icon-hint{background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:0 -45px;width:20px;height:20px;background-repeat:no-repeat}.statement-body h1 span,.statement-body h2 span{display:table-cell;vertical-align:middle}.statement-body span.var,.statement-body var{font-size:12px;padding:1px 4px;background-color:#f2bb13;color:#454c55;font-style:normal;font-weight:400;display:inline-block;border-radius:3px}.statement-body const,.statement-body span.const{color:#18a1ea;padding-left:2px;padding-right:2px;padding-top:2px;font-size:12px;font-weight:700}.statement-body action{display:inline-block;padding:0 4px;background-color:#18a1ea;white-space:nowrap;margin:0;color:#fff;font-size:12px;font-weight:400;border-radius:3px}.statement-body .statement-section{padding-left:15px;padding-right:15px;padding-bottom:30px}.statement-body .statement-protocol{color:#000;padding-top:30px;padding-bottom:30px;border-top:1px solid #dadada;background-color:#e7e9eb}.statement-body .statement-protocol .title{font-size:14px;font-weight:700;padding-top:5px;padding-bottom:15px}.statement-body .statement-protocol .blk{border-bottom:1px solid #dadada;padding-top:15px;padding-bottom:15px}.statement-body .statement-protocol .blk:last-child{border-bottom:none!important;padding-bottom:0}.statement-body .statement-protocol .blk:first-of-type{padding-top:0}.statement-body .statement-lineno{color:#838891;font-weight:700}.statement-body .statement-victory-conditions{color:#1a99aa;background-color:rgba(26,153,170,.1);padding:20px;margin-top:10px;display:-webkit-flex;display:flex;-webkit-align-items:center;align-items:center}.statement-body .statement-victory-conditions .icon{vertical-align:middle;text-align:center;margin-left:8px;margin-right:25px;min-width:25px;background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:-25px 0;width:25px;height:25px;background-repeat:no-repeat}.statement-body .statement-victory-conditions .blk{vertical-align:middle}.statement-body .statement-victory-conditions .title{font-weight:700;margin-bottom:10px}.statement-body .statement-lose-conditions{color:#f85338;background-color:rgba(248,83,56,.1);padding:20px;margin-top:10px;display:-webkit-flex;display:flex;-webkit-align-items:center;align-items:center}.statement-body .statement-lose-conditions .icon{vertical-align:middle;text-align:center;margin-left:8px;margin-right:25px;min-width:25px;background-image:url(https://static.codingame.com/assets/spritesheets/statement.8f64ade5.png);background-position:0 0;width:25px;height:25px;background-repeat:no-repeat}.statement-body .statement-lose-conditions .blk{vertical-align:middle}.statement-body .statement-lose-conditions .title{font-weight:700;margin-bottom:10px}.statement-body .statement-story-background{position:relative;background-color:#454c55;width:100%}.statement-body .statement-story-background img{width:100%;display:block}.statement-body .statement-story-background .statement-story-cover{background-size:cover}.statement-body .statement-story{top:0;left:0;height:100%;width:100%;padding:30px 15px;background-color:rgba(69,76,85,.8);text-align:justify;color:#fff}.statement-body .statement-story h1{font-weight:700;margin-top:0;color:#fff}.statement-body .statement-inout{display:-webkit-flex;display:flex;width:calc(100% + 10px);margin-left:-5px;margin-right:0;padding:0}.statement-body .statement-inout .statement-inout-in,.statement-body .statement-inout .statement-inout-out{-webkit-flex:1;flex:1;-webkit-flex-basis:auto;flex-basis:auto;color:#454c55;margin:5px;padding:10px;background-color:#fff;max-width:50%}.statement-body .statement-inout .statement-inout-in pre,.statement-body .statement-inout .statement-inout-out pre{scrollbar-color:rgba(0,0,0,.2) transparent;scrollbar-width:thin}.statement-body .statement-inout .statement-inout-in pre::-webkit-scrollbar,.statement-body .statement-inout .statement-inout-out pre::-webkit-scrollbar{width:14px;height:14px;background-color:transparent}.statement-body .statement-inout .statement-inout-in pre::-webkit-scrollbar-thumb,.statement-body .statement-inout .statement-inout-out pre::-webkit-scrollbar-thumb{min-height:40px;border:4px solid transparent;background-clip:padding-box;-webkit-border-radius:7px;background-color:rgba(0,0,0,.2)}.statement-body .statement-inout .statement-inout-in pre::-webkit-scrollbar-corner,.statement-body .statement-inout .statement-inout-out pre::-webkit-scrollbar-corner{background:0 0}.statement-body .statement-inout .statement-inout-in .title,.statement-body .statement-inout .statement-inout-out .title{font-weight:700;margin-bottom:10px;padding:0;color:#838891}.statement-body .statement-inout pre{border:none;background-color:transparent;font-family:inconsolata,monospace;font-size:14px;line-height:14px;font-weight:400;margin:0;padding:0;display:inline-block;width:100%;overflow-x:auto}.statement-body .statement-example-container{display:-webkit-flex;display:flex;-webkit-flex-wrap:wrap;flex-wrap:wrap;-webkit-justify-content:center;justify-content:center;margin-left:-10px;margin-right:-10px}.statement-body .statement-example{display:inline-block;width:260px;max-width:300px;-webkit-flex-basis:260px;flex-basis:260px;-webkit-flex-grow:1;flex-grow:1;background-color:#e7e9eb;margin:10px;vertical-align:top}.statement-body .statement-example .legend{color:#454c55;padding:15px;height:90px;display:block}.statement-body .statement-example .title{font-weight:700}.statement-body .statement-example img{background-color:#454c55;width:100%;display:block}.statement-body .statement-example.statement-example-empty,.statement-body .statement-example:empty{margin-top:0;margin-bottom:0;height:0}.statement-body .statement-example.statement-example-medium{-webkit-flex-basis:190px;flex-basis:190px;width:190px}.statement-body .statement-example.statement-example-small{-webkit-flex-basis:140px;flex-basis:140px;width:140px}.statement-body .statement-example-horizontal{width:100%;margin-top:10px;margin-bottom:10px;background-color:#e7e9eb;display:table}.statement-body .statement-example-horizontal .header{width:270px;display:table-cell;background-color:transparent;vertical-align:middle;padding:0}.statement-body .statement-example-horizontal .legend{color:#454c55;display:table-cell;vertical-align:middle;padding:10px 10px 10px 15px}.statement-body .statement-example-horizontal .title{font-weight:700}.statement-body .statement-example-horizontal img{background-color:#454c55;display:block}.statement-body .statement-new-league-rule{padding-left:10px;border-left:5px solid #7cc576;margin-left:-15px;background:rgba(124,197,118,.1);margin-right:-15px;padding-right:10px}.statement-body .statement-summary-new-league-rules{color:#7cc576;background-color:rgba(124,197,118,.1);padding:20px;margin-right:15px;margin-left:15px;margin-bottom:10px;text-align:left}.statement-body .statement-summary-new-league-rules .statement-summary-new-league-rules-logo{text-align:center;margin-bottom:6px}.statement-body .statement-summary-new-league-rules .statement-summary-new-league-rules-title{text-align:center;font-weight:700;margin-bottom:6px}.hide-protocol .statement-protocol{display:none}.hide-protocol .statement-goal{margin-bottom:0}.marked{color:#fff}.marked a,.marked a:active,.marked a:hover,.marked a:visited{border-bottom:none}.marked a,.marked a:active,.marked a:visited{color:#1a99aa}.marked a:hover{color:rgba(26,153,170,.8)}.marked blockquote{border-color:#363e48}.marked blockquote p{color:#838891}.marked table{background:#363e48}.marked table,.marked td,.marked th{border:1px solid #41454a}a,a:active,a:hover,a:visited{color:#fff;border-bottom:1px dotted #fff}.statement-body{background-color:#252e38;color:#fff}.statement-body .statement-victory-conditions{background-color:rgba(26,153,170,.15)}.statement-body .statement-lose-conditions{background-color:rgba(248,83,56,.15)}.statement-body .statement-protocol{color:#fff;background-color:#363e48;border-top:1px solid #41454a}.statement-body .statement-protocol .title{color:#838891}.statement-body .statement-protocol .blk{border-bottom:1px solid #41454a}.statement-body .statement-example .legend,.statement-body .statement-example-horizontal .legend{color:#fff;background-color:#363e48}.statement-body .statement-example-horizontal{background-color:#363e48}.statement-body .statement-inout-in,.statement-body .statement-inout-out{background-color:#454c55!important;color:#fff}.statement-body .statement-inout-in pre,.statement-body .statement-inout-out pre{scrollbar-color:hsla(0,0%,100%,.2) transparent;scrollbar-width:thin}.statement-body .statement-inout-in pre::-webkit-scrollbar,.statement-body .statement-inout-out pre::-webkit-scrollbar{width:14px;height:14px;background-color:transparent}.statement-body .statement-inout-in pre::-webkit-scrollbar-thumb,.statement-body .statement-inout-out pre::-webkit-scrollbar-thumb{min-height:40px;border:4px solid transparent;background-clip:padding-box;-webkit-border-radius:7px;background-color:hsla(0,0%,100%,.2)}.statement-body .statement-inout-in pre::-webkit-scrollbar-corner,.statement-body .statement-inout-out pre::-webkit-scrollbar-corner{background:0 0}.statement-container{scrollbar-color:hsla(0,0%,100%,.2) transparent;scrollbar-width:thin}.statement-container::-webkit-scrollbar{width:14px;height:14px;background-color:#454c55}.statement-container::-webkit-scrollbar-thumb{min-height:40px;border:4px solid transparent;background-clip:padding-box;-webkit-border-radius:7px;background-color:hsla(0,0%,100%,.2)}.statement-container::-webkit-scrollbar-corner{background:0 0}.statement_wrapping_div{color:#fff}
</style>
</head>
<body style='font-family:Open Sans,Lato,sans-serif!important;margin:0'>
$content
</body>
</html>"
}

function templateBody($content) {
    return "
<div id='statement_back' class='statement_back' style='display:none'></div>
<div class='statement-body'>
$content`n
</div>"
}

function templateHighlight($content) {
    Write-Output ""
    Write-Output "<div class='statement-summary-new-league-rules'>"
    Write-Output ""
    Write-Output "$content"
    Write-Output ""
    Write-Output "</div>"
    Write-Output ""
}

function getLayout($node, $accepted) {
    if ($null -ne $accepted) {
        $title = $node.title;
        foreach ($layout in $accepted) {
            if ($title -match $layout.token) {
                return $layout;
            }
        }
    }
    return @{ token = ''; name = 'default'; render = $DEFAULT_PLAIN; accepts = @() }
}

function drop ($content) {
    return "<!--
$content
-->"
}

function renderPlainContent($content) {
    $content = pimpMd -content $content | Out-String;
    $content = ($content  | ConvertFrom-Markdown).Html;
    Write-Output $content;
}
function renderNode ($node, $accepted, $parameters) {
    Write-Verbose "render $('#'*$node.level) $($node.title)";
    $gate = getGate -node $node -parameters $parameters
    if ($gate.open) {
        $layout = getLayout -accepted $accepted -node $node;

        $innerContent = renderPlainContent -content $node.content;
        if ($null -ne $node.children) {
            $innerContent += $node.children | % {
                renderNode -accepted $layout.accepts -node $_ -parameters $parameters
            }
        }
        $explicitNew = $node.title -match $NEW_TOKEN;
        $title = $node.title -replace $NEW_TOKEN, '';
        $title = $title -replace $layout.token, '';
        $title = $title -replace $MATCH_CONDITIONAL_STATEMENT_REGEX, '';


        $content += $layout.render.invoke($node, $layout, $title, $innerContent);
        if ($gate.new) {
            Write-Verbose "Node is new due to the parameters."
            templateHighlight -content $content;
        }
        elseif ($explicitNew){
            Write-Verbose "Node is new due to the explicit token '$NEW_TOKEN'."
            templateHighlight -content $content;
        }
        else {
            Write-Output $content
        }
    }
    else {
        Write-Verbose "Node ignored due to the parameters."
    }
}

function renderDocument ($document, $parameters) {

    if ($document.children.Length -ne 1) {
        Write-Error "Excepted one H1: found $($document.children.Length)";
        exit 1;
    }

    $html = "";
    $body = $document.children[0];
    $html += drop $document.content;
    $html += drop $body.title;
    $html += drop $body.content;

    Write-Verbose "render # $($body.title)";
    foreach ($node in $body.children) {
        $html += renderNode -accepted $LAYOUT -node $node -parameters $parameters;
    }
    $html = templateBody -content $html
    return $html
}

function export ($tree, $league, $subfolder, $debug, $release) {
    $default_name = "statement_$Language.html"
    $leagueLabel = if ($league -ge 256) { "default" } else { $league };
    Write-Host "Render document for the league '$leagueLabel' ...";
    $html = renderDocument -document $tree -parameters @{'LEAGUE' = $league };
    if ($release) {
        $folder = "$ReleaseDestination/$($subfolder)"
        mkdir $folder -ErrorAction SilentlyContinue | Out-Null
        $file = "$($folder)$default_name";
        Write-Host " -> Write  $file...";
        $html | Out-File -Path $file -Encoding utf8;
    }
    if ($debug) {
        $folder = "$ReviewDestination/$($subfolder)"
        mkdir $folder -ErrorAction SilentlyContinue | Out-Null
        $file = "$($folder)$default_name";
        Write-Host " -> Write  $file...";
        $htmlDebug = templateHtml -content $html;
        $htmlDebug | Out-File -Path $file -Encoding utf8;
    }
}

function main() {
    $release = -not [String]::IsNullOrWhiteSpace($ReleaseDestination);
    $debug = -not [String]::IsNullOrWhiteSpace($ReviewDestination);
    if (-not (Test-Path $Source -PathType Leaf)) {
        Write-Error "the source:$source doesn't exist: create the file or check the path";
        exit 1;
    }
    elseif (-not $release -and -not $debug) {
        Write-Warning "no actions required: define -ReleaseDestination AND/OR -ReviewDestination";
        exit 1;
    }
    Write-Debug "Parrameters source=$source release=$release review=$debug ...";


    Write-Host  "Read $Source ..."
    $raw = Get-Content $Source -Encoding utf8;

    Write-Host  "Parse the layout ..."
    $tree = parseSummary -md $raw -parameters @{'LEAGUE' = 0 };

    Write-Host "--------------------------"
    Write-Host "        LAYOUT            "
    Write-Host "--------------------------"
    debugSummary -node $tree | Write-Host
    Write-Host "--------------------------"

    # default league
    export -tree $tree -subfolder '' -league 256 -debug $debug -release $release;

    if ($Leagues -gt 0) {
        1..($Leagues) | % {
            export -tree $tree -subfolder "level$_/" -league $_ -debug $debug -release $release;
        }
    }
}

main;