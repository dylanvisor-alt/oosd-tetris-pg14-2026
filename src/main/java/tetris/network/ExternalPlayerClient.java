package tetris.network;

import com.google.gson.Gson;
import tetris.model.Board;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ExternalPlayerClient {

    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private final Gson gson = new Gson();

    public ExternalMove requestMove(
            int[][] cells,
            int[][] currentShape,
            int[][] nextShape) throws IOException {

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 1000);
            socket.setSoTimeout(2000);

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream(),
                            StandardCharsets.UTF_8));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),
                            StandardCharsets.UTF_8));

            GameSnapshot game = new GameSnapshot(
                    Board.WIDTH,
                    Board.HEIGHT,
                    cells,
                    currentShape,
                    nextShape);

            writer.write(gson.toJson(game));
            writer.newLine();
            writer.flush();

            String response = reader.readLine();

            if (response == null) {
                throw new IOException("No response from Tetris server");
            }

            return gson.fromJson(response, ExternalMove.class);
        }
    }

    private record GameSnapshot(
            int width,
            int height,
            int[][] cells,
            int[][] currentShape,
            int[][] nextShape) {
    }

    public record ExternalMove(int opX, int opRotate) {
    }
}