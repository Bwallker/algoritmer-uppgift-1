import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.util.Arrays;
import java.util.Random;

public class Ex1 {
	private static final int WIDTH = 800; // Size of the window in pixels
	private static final int HEIGHT = 800;

	static int cells = 20; // The size of the maze is cells*cells (default is 20*20)

	public static void main(String[] args) {

		// Get the size of the maze from the command line
		if (args.length > 0) {
			try {
				cells = Integer.parseInt(args[0]); // The maze is of size cells*cells
			} catch (NumberFormatException e) {
				System.err.println("Argument " + args[0] + " should be an integer");
				System.exit(-1);
			}
		}
		// Check that the size is valid
		if ((cells <= 1) || (cells > 100)) {
			System.err.println("Invalid size, must be between 2 and 100 ");
			System.exit(-1);
		}
		Runnable r = new Runnable() {
			public void run() {
				// Create a JComponent for the maze
				MazeComponent mazeComponent = new MazeComponent(WIDTH, HEIGHT, cells);
				// Change the text of the OK button to "Close"
				UIManager.put("OptionPane.okButtonText", "Close");
				JOptionPane.showMessageDialog(null, mazeComponent, "Maze " + cells + " by " + cells,
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(r);
	}
}

final class MazeComponent extends JComponent {
	private final int width;
	private final int height;
	private final int cells;
	private final int cellWidth;
	private final int cellHeight;
	private final Random random;

	// Draw a maze of size w*h with c*c cells
	MazeComponent(int w, int h, int c) {
		super();
		cells = c; // Number of cells
		cellWidth = w / cells; // Width of a cell
		cellHeight = h / cells; // Height of a cell
		width = c * cellWidth; // Calculate exact dimensions of the component
		height = c * cellHeight;
		random = new Random();
		setPreferredSize(new Dimension(width + 1, height + 1)); // Add 1 pixel for the border
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.yellow); // Yellow background
		g.fillRect(0, 0, width, height);
		// Draw a grid of cells
		g.setColor(Color.blue); // Blue lines
		for (int i = 0; i <= cells; i++) { // Draw horizontal grid lines
			g.drawLine(0, i * cellHeight, cells * cellWidth, i * cellHeight);
		}
		for (int j = 0; j <= cells; j++) { // Draw verical grid lines
			g.drawLine(j * cellWidth, 0, j * cellWidth, cells * cellHeight);
		}

		// Mark entry and exit cells
		paintCell(0, 0, Color.green, g); // Mark entry cell
		drawWall(-1, 0, 2, g); // Open up entry cell
		paintCell(cells - 1, cells - 1, Color.pink, g); // Mark exit cell
		drawWall(cells - 1, cells - 1, 2, g); // Open up exit cell

		g.setColor(Color.yellow); // Use yellow lines to remove existing walls
		createMaze(cells, g);
	}

	private boolean wallIsInvalid(int x, int y, int wall) {
		switch (wall) {
			// Left wall, can't be on the left edge.
			case 0:
				return x == 0;
			// Upper wall, can't be on the top edge.
			case 1:
				return y == 0;
			// Right wall, can't be on the right edge.
			case 2:
				return x == cells - 1;
			// Lower wall, can't be on the bottom edge.
			case 3:
				return y == cells - 1;
			default:
				throw new IllegalArgumentException("Invalid wall: " + wall);
		}
	}

	private int calculateIndex(int index, int wall) {
		switch (wall) {
			case 0:
				return index - 1;
			case 1:
				return index - cells;
			case 2:
				return index + 1;
			case 3:
				return index + cells;
			default:
				throw new IllegalArgumentException("Invalid wall: " + wall);
		}
	}

	private void createMaze(int cells, Graphics g) {
		// Get the current color of the graphics object so we can restore it later.
		Color c = g.getColor();
		// Set the color to our background color so we can remove walls by painting over
		// them with the background color.
		g.setColor(Color.yellow);
		UnionFind uf = new UnionFind(cells * cells);
		while (!uf.isUniform()) {
			int index = random.nextInt(cells * cells);
			int wall = random.nextInt(4);
			if (wallIsInvalid(index % cells, index / cells, wall)) {
				wall = (wall + 2) % 4;
			}
			int index2 = calculateIndex(index, wall);
			if (uf.find(index) == uf.find(index2)) {
				continue;
			}
			drawWall(index % cells, index / cells, wall, g);
			uf.union(index, index2);
		}

		// Undo the color change.
		g.setColor(c);
	}

	// Paints the interior of the cell at postion x,y with colour c
	private void paintCell(int x, int y, Color c, Graphics g) {
		int xpos = x * cellWidth; // Position in pixel coordinates
		int ypos = y * cellHeight;
		g.setColor(c);
		g.fillRect(xpos + 1, ypos + 1, cellWidth - 1, cellHeight - 1);
	}

	// Draw the wall w in cell (x,y) (0=left, 1=up, 2=right, 3=down)
	private void drawWall(int x, int y, int w, Graphics g) {
		int xpos = x * cellWidth; // Position in pixel coordinates
		int ypos = y * cellHeight;

		switch (w) {
			case (0): // Wall to the left
				g.drawLine(xpos, ypos + 1, xpos, ypos + cellHeight - 1);
				break;
			case (1): // Wall at top
				g.drawLine(xpos + 1, ypos, xpos + cellWidth - 1, ypos);
				break;
			case (2): // Wall to the right
				g.drawLine(xpos + cellWidth, ypos + 1, xpos + cellWidth, ypos + cellHeight - 1);
				break;
			case (3): // Wall at bottom
				g.drawLine(xpos + 1, ypos + cellHeight, xpos + cellWidth - 1, ypos + cellHeight);
				break;
		}
	}
}

final class UnionFind {
	private int[] nodes;

	public UnionFind(int n) {
		nodes = new int[n];
		for (int i = 0; i < n; i++) {
			nodes[i] = -1;
		}
	}

	private int find_impl(int x) {
		if (nodes[x] < 0)
			return x;
		else
			return nodes[x] = find(nodes[x]);
	}

	public int find(int x) {
		if (x < 0 || x >= nodes.length) {
			throw new IllegalArgumentException("Index out of bounds");
		}
		return find_impl(x);
	}

	// Unions the sets that contain x and y.
	public void union(int x, int y) {
		if (x < 0 || x >= nodes.length || y < 0 || y >= nodes.length) {
			throw new IllegalArgumentException("Index out of bounds");
		}
		x = find(x);
		y = find(y);
		if (nodes[y] < nodes[x]) {
			nodes[y] += nodes[x];
			nodes[x] = y;
		} else {
			nodes[x] += nodes[y];
			nodes[y] = x;
		}
	}

	// Counts how many roots there are in the UnionFind and returns the count.
	public int numRoots() {
		int count = 0;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] < 0) {
				count++;
			}
		}
		return count;
	}

	// Returns true if the UnionFind is uniform, meaning that there is only one
	// root.
	public boolean isUniform() {
		return numRoots() == 1;
	}

	@Override
	public String toString() {
		return Arrays.toString(nodes);
	}
}
