
$conf = @(
    @{source = '$PSScriptRoot/../rules_en.md'; leagues = 3; language='en' }
    @{source = '$PSScriptRoot/../rules_fr.md'; leagues = 3; language='fr'}
)

$ReviewDestination = "$PSScriptRoot/../.review";
$ReleaseDestination = "$PSScriptRoot/../config";

$conf | % {
    $source = $_.source;
    . "$PSScriptRoot/markdown2cg.ps1" -Source $source -ReviewDestination $ReviewDestination -ReleaseDestination $ReleaseDestination -Leagues $_.leagues -Language $_.language;
}