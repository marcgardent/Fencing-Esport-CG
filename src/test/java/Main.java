import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class Main {
    public static void main(String[] args) {
        MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
        int league = 3;
        //gameRunner.addAgent(MessageFormat.format("D:\\tmp\\cg-rust-playground\\dist\\artifacts\\league1.exe ", league));
        //gameRunner.addAgent(MessageFormat.format("D:\\tmp\\cg-rust-playground\\dist\\artifacts\\league{0}.exe ", league));
        gameRunner.addAgent(Player.class);
        gameRunner.addAgent(Player2.class);
        gameRunner.setLeagueLevel(league);
        gameRunner.start();
    }
}