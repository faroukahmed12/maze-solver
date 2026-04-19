import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MazeGUI extends JFrame {

    // ── Fix 3: all inner classes are now static so static methods can use them ──

    static class Node {
        int x, y;
        Node parent;
        int g, h;

        Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }

        int f() { return g + h; }
    }

    static class Maze {
        int[][] grid = {
                {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0},
                {0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0},
                {0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1},
                {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        int rows = grid.length;
        int cols = grid[0].length;

        boolean isValid(int x, int y) {
            return x >= 0 && y >= 0 && x < rows && y < cols && grid[x][y] == 0;
        }
    }

    static class Algorithms {

        static List<Node> BFS(Maze maze, int sx, int sy, int gx, int gy) {
            Queue<Node> q = new LinkedList<>();
            boolean[][] vis = new boolean[maze.rows][maze.cols];

            q.add(new Node(sx, sy, null));
            vis[sx][sy] = true;

            int[] dx = {0, 1, 0, -1};
            int[] dy = {1, 0, -1,  0};

            while (!q.isEmpty()) {
                Node cur = q.poll();
                if (cur.x == gx && cur.y == gy) return build(cur);

                for (int i = 0; i < 4; i++) {
                    int nx = cur.x + dx[i], ny = cur.y + dy[i];
                    if (maze.isValid(nx, ny) && !vis[nx][ny]) {
                        vis[nx][ny] = true;
                        q.add(new Node(nx, ny, cur));
                    }
                }
            }
            return null;
        }

        static List<Node> DFS(Maze maze, int sx, int sy, int gx, int gy) {
            Stack<Node> s = new Stack<>();
            boolean[][] vis = new boolean[maze.rows][maze.cols];

            s.push(new Node(sx, sy, null));

            int[] dx = {0, 1, 0, -1};
            int[] dy = {1, 0, -1,  0};

            while (!s.isEmpty()) {
                Node cur = s.pop();
                if (vis[cur.x][cur.y]) continue;
                vis[cur.x][cur.y] = true;

                if (cur.x == gx && cur.y == gy) return build(cur);

                for (int i = 0; i < 4; i++) {
                    int nx = cur.x + dx[i], ny = cur.y + dy[i];
                    if (maze.isValid(nx, ny) && !vis[nx][ny]) {
                        s.push(new Node(nx, ny, cur));
                    }
                }
            }
            return null;
        }

        static List<Node> AStar(Maze maze, int sx, int sy, int gx, int gy) {
            PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::f));
            boolean[][] vis = new boolean[maze.rows][maze.cols];

            Node start = new Node(sx, sy, null);
            start.g = 0;
            start.h = Math.abs(sx - gx) + Math.abs(sy - gy);
            open.add(start);

            int[] dx = {0, 1, 0, -1};
            int[] dy = {1, 0, -1,  0};

            while (!open.isEmpty()) {
                Node cur = open.poll();

                if (cur.x == gx && cur.y == gy) return build(cur);
                if (vis[cur.x][cur.y]) continue;
                vis[cur.x][cur.y] = true;

                for (int i = 0; i < 4; i++) {
                    int nx = cur.x + dx[i], ny = cur.y + dy[i];
                    if (maze.isValid(nx, ny) && !vis[nx][ny]) {
                        Node n = new Node(nx, ny, cur);
                        n.g = cur.g + 1;
                        n.h = Math.abs(nx - gx) + Math.abs(ny - gy);
                        open.add(n);
                    }
                }
            }
            return null;
        }

        static List<Node> build(Node n) {
            List<Node> path = new ArrayList<>();
            while (n != null) { path.add(n); n = n.parent; }
            Collections.reverse(path);
            return path;
        }
    }

    // ── Fix 1: MazeGUI itself is now the JFrame subclass (no duplicate name) ──

    Maze maze = new Maze();
    JButton[][] cells;
    Point start = null, goal = null;

    public MazeGUI() {
        setTitle("AI Maze Solver");
        setSize(600, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel gridPanel = new JPanel(new GridLayout(maze.rows, maze.cols));
        cells = new JButton[maze.rows][maze.cols];

        for (int i = 0; i < maze.rows; i++) {
            for (int j = 0; j < maze.cols; j++) {
                JButton btn = new JButton();
                btn.setOpaque(true);
                btn.setBackground(maze.grid[i][j] == 1 ? Color.BLACK : Color.WHITE);

                int x = i, y = j;
                btn.addActionListener(e -> handleClick(x, y));
                cells[i][j] = btn;
                gridPanel.add(btn);
            }
        }

        JPanel control = new JPanel();
        JButton bfs   = new JButton("BFS");
        JButton dfs   = new JButton("DFS");
        JButton astar = new JButton("A*");
        JButton reset = new JButton("Reset");

        bfs.addActionListener(e   -> runAlgo("BFS"));
        dfs.addActionListener(e   -> runAlgo("DFS"));
        astar.addActionListener(e -> runAlgo("A*"));
        reset.addActionListener(e -> reset());

        control.add(bfs);
        control.add(dfs);
        control.add(astar);
        control.add(reset);

        add(gridPanel, BorderLayout.CENTER);
        add(control,   BorderLayout.SOUTH);
    }

    void handleClick(int x, int y) {
        if (maze.grid[x][y] == 1) return;

        if (start == null) {
            start = new Point(x, y);
            cells[x][y].setBackground(Color.BLUE);
        } else if (goal == null) {
            goal = new Point(x, y);
            cells[x][y].setBackground(Color.RED);
        }
    }

    void runAlgo(String type) {
        if (start == null || goal == null) {
            JOptionPane.showMessageDialog(this, "Select Start & Goal first!");
            return;
        }

        List<Node> path;
        if      (type.equals("BFS")) path = Algorithms.BFS(maze, start.x, start.y, goal.x, goal.y);
        else if (type.equals("DFS")) path = Algorithms.DFS(maze, start.x, start.y, goal.x, goal.y);
        else                         path = Algorithms.AStar(maze, start.x, start.y, goal.x, goal.y);

        if (path == null) {
            JOptionPane.showMessageDialog(this, "No Path Found!");
            return;
        }
        animate(path);
    }

    void animate(List<Node> path) {
        new Thread(() -> {
            try {
                for (Node n : path) {
                    if (!(n.x == start.x && n.y == start.y) &&
                            !(n.x == goal.x  && n.y == goal.y)) {
                        cells[n.x][n.y].setBackground(Color.GREEN);
                        Thread.sleep(200);
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    void reset() {
        start = null;
        goal  = null;
        for (int i = 0; i < maze.rows; i++)
            for (int j = 0; j < maze.cols; j++)
                cells[i][j].setBackground(maze.grid[i][j] == 1 ? Color.BLACK : Color.WHITE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeGUI().setVisible(true));
    }
}