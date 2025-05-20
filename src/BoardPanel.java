import java.awt.*;
import java.util.*;
import javax.swing.*;

public class BoardPanel extends JPanel {
    private Board board;
    private final Map<Character,Color> colorMap = new HashMap<>();
    private static final Color EMPTY    = Color.WHITE;
    private static final Color OBSTACLE = Color.DARK_GRAY;
    private static final Color EXIT     = Color.GREEN;

    public BoardPanel() {
        setPreferredSize(new Dimension(360,360));
    }

    public void setBoard(Board b) {
        this.board = b;
        assignColors(b);
        repaint();
    }

    private void assignColors(Board b) {
        for (char id : b.pieces.keySet()) {
            colorMap.computeIfAbsent(id, k -> {
                if (k=='P') return Color.RED;
                float hue = ((k*13)%360)/360f;
                return Color.getHSBColor(hue,0.5f,0.9f);
            });
        }
        colorMap.put('X', OBSTACLE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;

        int R = board.rows, C = board.cols;
        int w = getWidth(), h = getHeight();
        int cellW = w / C, cellH = h / R;
        int offX = (w - C*cellW)/2, offY = (h - R*cellH)/2;

        // grid
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                char id = board.grid[r][c];
                Color col = switch (id) {
                    case 'X'      -> OBSTACLE;
                    case '.'      -> EMPTY;
                    default       -> colorMap.getOrDefault(id, EMPTY);
                };
                g.setColor(col);
                g.fillRect(offX + c*cellW, offY + r*cellH, cellW, cellH);
                g.setColor(Color.BLACK);
                g.drawRect(offX + c*cellW, offY + r*cellH, cellW, cellH);
            }
        }

        // exit
        g.setColor(EXIT);
        int er = board.exitRow, ec = board.exitCol;
        if (er == -1)                         // top
            g.fillRect(offX + ec*cellW, offY, cellW, cellH/4);
        else if (er == R)                     // bottom
            g.fillRect(offX + ec*cellW, offY + R*cellH - cellH/4, cellW, cellH/4);
        else if (ec == -1)                    // left
            g.fillRect(offX, offY + er*cellH, cellW/4, cellH);
        else if (ec == C)                     // right
            g.fillRect(offX + C*cellW - cellW/4, offY + er*cellH, cellW/4, cellH);
    }
}
