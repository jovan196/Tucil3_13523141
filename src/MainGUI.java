import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.swing.*;

public class MainGUI extends JFrame {
    private final JTextField fileField = new JTextField(20);
    private final JComboBox<String> algBox = new JComboBox<>(new String[]{"ucs", "gbfs", "astar"});
    private final JComboBox<Integer> hBox = new JComboBox<>(new Integer[]{1,2,3});
    private final BoardPanel boardPanel = new BoardPanel();
    private final JButton prevBtn = new JButton("Prev"), nextBtn = new JButton("Next");
    private List<State> path; private int currentIndex;
    private final JLabel statusLabel;
    private long execTime;
    private long visitedCount;
    private final JButton saveBtn = new JButton("Save Results");

    public MainGUI() {
        super("RushHour Solver");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel top = new JPanel();
        statusLabel = new JLabel("Node Dikunjungi: 0 Waktu: 0ms Langkah: 0/0");
        JButton browse = new JButton("Browse…");
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
                fileField.setText(fc.getSelectedFile().getAbsolutePath());
        });
        top.add(new JLabel("File:")); top.add(fileField); top.add(browse);
        top.add(new JLabel("Algoritma:")); top.add(algBox);
        top.add(new JLabel("H-ID:")); top.add(hBox);
        JButton solve = new JButton("Pecahkan");
        solve.addActionListener(e -> doSolve());
        top.add(solve);

        // add top controls without status label
        add(top, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        JPanel nav = new JPanel();
        prevBtn.addActionListener(e -> {
            if (path!=null && currentIndex>0) { currentIndex--; updateBoard(); }
        });
        nextBtn.addActionListener(e -> {
            if (path!=null && currentIndex<path.size()-1) { currentIndex++; updateBoard(); }
        });
        saveBtn.addActionListener(e -> doSave());
        nav.add(prevBtn); nav.add(nextBtn);
        nav.add(saveBtn);
        // combine navigation and status label at bottom
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(nav, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void doSolve() {
        try {
            String file = fileField.getText().trim();
            String alg  = ((String)algBox.getSelectedItem()).toLowerCase();
            int hid     = (Integer)hBox.getSelectedItem();
            Board start = Parser.load(file);
            PathFinder pf = switch (alg) {
                case "ucs"   -> new UniformCostSearch();
                case "gbfs"  -> new GreedyBestFirstSearch();
                case "astar" -> new AStarSearch();
                default      -> throw new IllegalArgumentException();
            };
            Heuristic h = Heuristics.byId(hid);
            // run search
            long t0 = System.currentTimeMillis();
            SearchResult res = pf.search(start, h);
            execTime = System.currentTimeMillis() - t0;
            visitedCount = res.visitedCount();
            // display as colored board steps
            path = res.path();
            currentIndex = 0;
            // show error if no solution found
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tidak ada solusi ditemukan.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            updateBoard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBoard() {
        boardPanel.setBoard(path.get(currentIndex).board);
        // update status with visited, time, and current step
        statusLabel.setText(String.format("Node Dikunjungi: %d Waktu: %dms Langkah: %d/%d", visitedCount, execTime, currentIndex+1, path.size()));
    }
   
    /** Prompt to save current results to a .txt file */
    private void doSave() {
        if (path == null) {
            JOptionPane.showMessageDialog(this, "No results to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Results");
        fc.setSelectedFile(new File("results.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = fc.getSelectedFile();
            // warn if file exists
            if (out.exists()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "File '" + out.getName() + "' already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            try (FileWriter fw = new FileWriter(out)) {
                fw.write(buildResultText());
                JOptionPane.showMessageDialog(this, "Results saved to " + out.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Build CLI-style output string for the current solution path */
    private String buildResultText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node Dikunjungi: ").append(visitedCount)
          .append("   Waktu (ms): ").append(execTime).append("\n\n");
        for (int i = 0; i < path.size(); i++) {
            State s = path.get(i);
            if (i == 0) sb.append("Awal:\n");
            else sb.append("Move ").append(i).append(": ").append(s.move).append("\n");
            // strip ANSI color codes before saving
            String plain = stripAnsi(Printer.pretty(s.board, s.move.piece));
            sb.append(plain).append("\n");
        }
        return sb.toString();
    }

    /** Remove ANSI escape codes from a string */
    private String stripAnsi(String s) {
        return s.replaceAll("\\x1B\\[[;\\d]*m", "");
    }
}
