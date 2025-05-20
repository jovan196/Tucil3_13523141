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
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board==null) return;
        int R = board.rows, C = board.cols;
        int w = getWidth(), h = getHeight();
        int cw = w/C, ch = h/R;
        // cells
        for(int r=0;r<R;r++) for(int c=0;c<C;c++){
            char chId = board.grid[r][c];
            Color col = EMPTY;
            if (chId=='X')      col = OBSTACLE;
            else if (chId!='.') col = colorMap.get(chId);
            g.setColor(col);
            g.fillRect(c*cw, r*ch, cw, ch);
            g.setColor(Color.BLACK);
            g.drawRect(c*cw, r*ch, cw, ch);
        }
        // exit marker
        g.setColor(EXIT);
        int er=board.exitRow, ec=board.exitCol;
        if (er==-1)      g.fillRect(ec*cw, 0, cw, ch/4);
        else if (er==R)  g.fillRect(ec*cw, R*ch-ch/4, cw, ch/4);
        else if (ec==-1) g.fillRect(0, er*ch, cw/4, ch);
        else if (ec==C)  g.fillRect(C*cw-cw/4, er*ch, cw/4, ch);
    }
}
