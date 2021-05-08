import com.codingame.gameengine.runner.MultiplayerGameRunner;
import java.text.MessageFormat;

public class Main {
    public static void main(String[] args) {
        MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
        int league = 1;
        gameRunner.addAgent(MessageFormat.format("D:\\Temp\\cg-rust-playground\\dist\\artifacts\\league{0}.exe ", league));
        gameRunner.addAgent(MessageFormat.format("D:\\Temp\\cg-rust-playground\\dist\\artifacts\\league{0}.exe ", league));
        gameRunner.setLeagueLevel(league);
        gameRunner.start();
    }
}