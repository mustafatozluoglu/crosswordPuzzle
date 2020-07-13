import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Puzzle {
    public Puzzle(int i, int j, boolean is_across, int length) {
        this.i = i;
        this.j = j;
        this.is_across = is_across;
        this.length = length;
    }

    int i;
    int j;
    boolean is_across; // if false, this is a down start
    int length;
}

public class CrosswordPuzzle {

    static int[][] puzzle;
    static final int OBSTACLE = -1;
    static final int EMPTY = 0;
    static final int ACROSS = 1;
    static final int DOWN = 2;
    static final int BOTH = 3;

    static ArrayList<String> wordsUsed = new ArrayList<>();
    static ArrayList<String> words = new ArrayList<>();


    public static void main(String[] args) throws IOException {

        BufferedReader file = new BufferedReader(new FileReader("englishWords.txt"));
        while (true) {
            String word = file.readLine();
            if (word == null) {
                break;
            }
            words.add(word);
        }

        System.out.println("Enter Table Size: ");
        Scanner scanner = new Scanner(System.in);
        int tableSize = scanner.nextInt();

        boolean[][] words = new boolean[tableSize][tableSize];

        // RUN LENGTH ORGANIZER
        lengthOrganizer("englishWords.txt");

        /** EXAMPLE:
         *  {true, true, true
         *   true, true, false
         *   true, false, true}
         *
         *   + | |
         *   -   W
         *   - W
         */
        for (int i = 0; i < tableSize; i++) {
            for (int j = 0; j < tableSize; j++) {
                words[i][j] = true;
            }
        }

        /*
        // RANDOM OBSTACLE
        Random rand = new Random();
        for (int i = tableSize - 1; i > -1; i--) {
            int randomOne = rand.nextInt(3);
            for (int j = randomOne; j > 0; j--) {
                words[i][rand.nextInt(tableSize)] = false;
            }

        }*/

        // CHOOSE OWN OBSTACLE
        words[0][0] = false;
        words[0][2] = false;
        words[0][4] = false;
        words[2][0] = false;
        words[3][0] = false;
        words[2][2] = false;
        words[2][3] = false;
        words[3][2] = false;
        words[3][3] = false;
        words[4][0] = false;


        puzzle = new int[words.length][words.length];
        char[][] table = new char[words.length][words.length];

        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[0].length; j++) {
                if (words[i][j] == false) {
                    puzzle[i][j] = OBSTACLE;
                } else {
                    puzzle[i][j] = EMPTY;
                }
            }
        }
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[0].length; j++) {
                int flag = 0;
                if (words[i][j] == false) {
                    continue;
                }
                if ((i == 0 || words[i - 1][j] == false) && (i < puzzle.length / 2 && words[i + 1][j] == true)) {
                    puzzle[i][j] = DOWN;
                    flag++;
                }
                if ((j == 0 || words[i][j - 1] == false) && (j < puzzle.length / 2 && words[i][j + 1] == true)) {
                    if (flag == 1) {
                        puzzle[i][j] = BOTH;
                    } else {
                        puzzle[i][j] = ACROSS;
                    }
                }
            }
        }

        System.out.println("Puzzle:");
        printTable(puzzle);
        System.out.println("------------------");

        ArrayList<Puzzle> word_start_list = new ArrayList<>();
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[0].length; j++) {
                if (puzzle[i][j] == ACROSS || puzzle[i][j] == BOTH) {
                    word_start_list.add(new Puzzle(i, j, true, WordLength(puzzle, i, j, true)));
                }
                if (puzzle[i][j] == DOWN || puzzle[i][j] == BOTH) {
                    word_start_list.add(new Puzzle(i, j, false, WordLength(puzzle, i, j, false)));
                }
            }
        }


        if (solvePuzzle(word_start_list, table, 0)) {
            System.out.println("Solution:");
            printSolution(puzzle, table);
            System.out.println("------------------");
            System.out.println("Total Point: " + findTotalPoint(wordsUsed));
        } else {
            System.out.println("No Solution!");
        }


    }

    static int WordLength(int[][] puzzle, int i, int j, boolean is_across) {
        int len;
        for (len = 1; len < puzzle.length; len++) {
            if (is_across) {
                j++;
            } else {
                i++;
            }
            if (i >= puzzle.length || j >= puzzle.length || puzzle[i][j] == OBSTACLE) {
                break;
            }
        }
        return len;
    }

    private static boolean solvePuzzle(ArrayList<Puzzle> puzzle, char[][] table, int cur_word_start) {
        if (cur_word_start >= puzzle.size()) {
            return true;
        }

        Puzzle ws = puzzle.get(cur_word_start);

        // Remember which characters in this word were already set
        boolean[] had_letter = new boolean[ws.length];
        for (int i = 0; i < ws.length; i++) {
            had_letter[i] = true;
        }
        int r = ws.i;
        int c = ws.j;
        for (int i = 0; i < ws.length; i++) {
            if (table[r][c] == 0) {
                had_letter[i] = false;
            }
            if (ws.is_across)
                c++;
            else
                r++;
        }

        // Find a word that fits here, given the letters already on the board
        for (String word : words) {
            if (!wordsUsed.contains(word) && word.length() == ws.length) {
                boolean word_fits = true;
                r = ws.i;
                c = ws.j;
                for (int j = 0; j < ws.length; j++) {
                    if (table[r][c] != 0 && table[r][c] != word.charAt(j)) {
                        word_fits = false;
                        break;
                    }
                    if (ws.is_across)
                        c++;
                    else
                        r++;
                }

                if (word_fits) {
                    // Place this word on the board
                    wordsUsed.add(word);
                    r = ws.i;
                    c = ws.j;
                    for (int j = 0; j < ws.length; j++) {
                        table[r][c] = word.charAt(j);
                        if (ws.is_across)
                            c++;
                        else
                            r++;
                    }

                    // If puzzle can be solved this way, we're done
                    if (solvePuzzle(puzzle, table, cur_word_start + 1)) {
                        return true;
                    }

                    // If not, take up letters that we placed and try a different word
                    r = ws.i;
                    c = ws.j;
                    for (int j = 0; j < ws.length; j++) {
                        if (!had_letter[j])
                            table[r][c] = 0;
                        if (ws.is_across)
                            c++;
                        else
                            r++;
                    }

                    wordsUsed.remove(word);
                }
            }
        }

        // If no word can work, return false.
        return false;

    }

    private static void printSolution(int[][] puzzle, char[][] table) {
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[0].length; j++) {
                int ws = puzzle[i][j];
                if (ws == OBSTACLE) {
                    System.out.print("_ ");
                } else {
                    System.out.print(table[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    static void printTable(int[][] puzzle) {
        for (int[] ints : puzzle) {
            for (int j = 0; j < puzzle[0].length; j++) {
                int ws = ints[j];
                if (ws == OBSTACLE) {
                    System.out.print("W ");
                }
                if (ws == EMPTY) {
                    System.out.print("  ");
                }
                if (ws == ACROSS) {
                    System.out.print("- ");
                }
                if (ws == DOWN) {
                    System.out.print("| ");
                }
                if (ws == BOTH) {
                    System.out.print("+ ");
                }
            }
            System.out.println();
        }
    }

    public static void lengthOrganizer(String filename) throws IOException { // find words length
        System.out.println("------------------\nLength of Words: ");

        BufferedReader f = new BufferedReader(new FileReader(filename));

        int[] thing = new int[100000];
        while (true) {
            String word = f.readLine();
            if (word == null)
                break;
            thing[word.length()]++;
        }
        for (int i = 0; i < 50; i++) {
            if (thing[i] != 0)
                System.out.println("Length[" + i + "]" + ": " + thing[i]);
        }

        System.out.println("------------------");
    }

    public static int findTotalPoint(ArrayList<String> wordsUsed) {
        int totalPoint = 0;

        int wordPoint = 0;
        for (String word : wordsUsed) {
            for (int i = 0; i < word.length(); i++) {
                wordPoint += (int) word.charAt(i);
            }
            totalPoint += wordPoint;
            wordPoint = 0;
        }

        return totalPoint;
    }


}

