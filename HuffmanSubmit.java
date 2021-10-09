/* Code & Commentary by Grayson Westfall, 2021 */

import java.io.*;

public class HuffmanSubmit implements Huffman {
	
	// Number of ASCII characters / used for arrays
	private static final int R = 256;
	
	// Node class used to build the trie structure
	private class Node implements Comparable<Node> {
		
		private char character; // character held in node
		private int charFrequency; // frequency of the character (or of all characters in subtree)
		private Node left; // pointer to left child of node
		private Node right; // pointer to right child of node
		
		// Constructor to initialize values
		Node(char character, int charFrequency, Node left, Node right) {
			
			this.character = character;
			this.charFrequency = charFrequency;
			this.left = left;
			this.right = right;
		}
		
		// Returns true if the node is a leaf node
		public boolean isLeaf() { return left == null && right == null; }
		
		// Compares frequencies of nodes to help build the minimum PQ
		public int compareTo(Node otherNode) {
			return this.charFrequency - otherNode.charFrequency;
		}
	}

	// Minimum priority queue used to help build Huffman trie
	private class MinPriorityQueue<Item extends Comparable<Item>> {
		
		private Item[] pq; // Array of values that represents the queue
		private int N = 0; // Number of items in the queue
		
		// Constructor that takes the array size as a parameter
		public MinPriorityQueue(int minN) {
			pq = (Item[]) new Comparable[minN + 1];
		}
		
		// Returns true if no items are in queue
		public boolean isEmpty() {
			return N == 0;
		}
		
		// Returns number of items in queue
		public int size() {
			return N;
		}
		
		/* Inserts item into the last position of the queue
		 * and calls swim() on it to move it to the proper one */
		public void insert(Item v) {
			pq[++N] = v;
			swim(N);
		}
		
		/* Deletes the first item from the queue by exchanging it
		 * with the last item and decrementing N. Moves the new
		 * first item to its proper position by calling sink on it */
		public Item delMin() {
			Item min = pq[1];
			exchange(1, N--);
			pq[N+1] = null;
			sink(1);
			return min; // returns the deleted value
		}
		
		/* Returns true if the item at position i in the
		 * queue is 'greater' than the item at position j */
		private boolean greater(int i, int j) {
			return pq[i].compareTo(pq[j]) > 0; 
		}
		
		// Exchanges items at positions i and j
		private void exchange(int i, int j) {
			Item t = pq[i];
			pq[i] = pq[j];
			pq[j] = t;
		}
		
		/* Repeatedly exchanges the item on
		 * which swim was called with its parent
		 * until it reaches the proper position */
		private void swim(int k) {
			while (k > 1 && greater(k/2, k)) {
				exchange(k/2, k);
				k = k/2;
			}
		}
		
		/* Repeatedly exchanges the item on which
		 * swim was called with its greater child
		 * until it reaches the proper position */
		private void sink(int k) {
			while (2*k <= N) {
				
				int j = 2*k;
				if (j < N && greater(j, j+1)) {
					++j;
				}
				if (!greater(k, j)) {
					break;
				}
				exchange(k, j);
				k = j;
			}
		}
	}
	
	// Builds array of all character frequencies
	private int[] buildFreqArray(char[] charArray) {
		int[] freqArray = new int[R];
		
		for (int i = 0; i < charArray.length; ++i) {
			freqArray[charArray[i]] += 1;
		}
		
		return freqArray;
	}	
	
	/* Method that create the Huffman trie
	 * and returns the root node of the trie */
	private Node buildHuffmanTrie(int[] freqArray) {
		
		/* MinPQ keeps characters with lower
		 * frequencies at the top of the queue */
		MinPriorityQueue<Node> pq = new MinPriorityQueue<Node>(R);
		for (char c = 0; c < R; ++c) {
			if (freqArray[c] > 0) {
				// Inserts a leaf node containing each character and its frequency
				pq.insert(new Node(c, freqArray[c], null, null));
			}
		}
		
		/* Removes the two minimums of the queue and creates
		 * a parent node for the two with their combined
		 * frequency and reinserts it */
		while (pq.size() > 1) {
			
			Node x = pq.delMin();
			Node y = pq.delMin();
			Node parent = new Node('\0', x.charFrequency + y.charFrequency, x, y);
			pq.insert(parent);
		}
		
		/* Once the size of the queue is 1, we have the
		 * root node, which is deleted and returned */
		return pq.delMin();
	}
	
	/* Extra method to call the recursive buildCode method,
	 * returns the array of codes that match to characters */
	private String[] buildCode(Node root) {
		
		String[] codeStrings = new String[R];
		buildCode(codeStrings, root, ""); // starts each code with an empty string
		return codeStrings; // returns the array of codes
	}
	
	// Recursively builds the coded strings for each character
	private void buildCode(String[] codeStrings, Node x, String s) {
		
		/* If the node is a leaf, we've reached the character
		 * and can put the coded string into the array */
		if (x.isLeaf()) {
			codeStrings[x.character] = s;
			return;
		}
		
		// Recursively moves to the next node to either left or right
		buildCode(codeStrings, x.left, s + '0'); // Adds 0 to the string if moving to left
		buildCode(codeStrings, x.right, s + '1'); // Adds 1 to the string if moving to right
	}

	/* Writes the values in the trie to a freqFile which can be 
	 * used to rebuild the same trie later for decryption */
	private void writeTrie(Node x, int[] freqArray, BufferedWriter trieWriter) throws IOException {
		
		int numCharacters = 0;
		
		// Counts all characters in the encrypted file 
		for (char c = 0; c < R; ++c) {
			numCharacters += freqArray[c];
		}
		
		// Writes numCharacters to the freqFile to be used for decryption
		trieWriter.write(String.valueOf(numCharacters));
		trieWriter.newLine();
		trieWriter.flush();
		
		/* Writes each character as a binary string to
		 * freqFile along with the character's frequency */
		for (char c = 0; c < R; ++c) {
			if (freqArray[c] != 0) {
				trieWriter.write(Integer.toBinaryString(c) + ":" + freqArray[c]);
				trieWriter.newLine();
				trieWriter.flush();
			}
		}

	}
	
	/* Reads values from the freqFile to rebuild
	 * the trie from a previously encrypted file */
	private Node readTrie(BufferedReader trieInput) throws IOException {
		
		// Rebuilds the old Huffman trie using the MinPQ again
		MinPriorityQueue<Node> pq = new MinPriorityQueue<Node>(R);
		
		String nextLine = trieInput.readLine();
		while (nextLine != null) {
			String binaryString = nextLine.substring(0, nextLine.indexOf(":")); // Isolates binary representation of character
			char c = (char) Integer.parseInt(binaryString, 2); // Casts binary representation as the actual character
			int charFrequency = Integer.parseInt(nextLine.substring(nextLine.indexOf(":") + 1)); // Isolates character's frequency
			
			pq.insert(new Node(c, charFrequency, null, null));
			
			nextLine = trieInput.readLine();
		}
		
		while (pq.size() > 1) {
			
			Node x = pq.delMin();
			Node y = pq.delMin();
			Node parent = new Node('\0', x.charFrequency + y.charFrequency, x, y);
			pq.insert(parent);
		}

		return pq.delMin();
	}
	
	// Encryption method that utilizes previously built helper methods
	public void encode(String inputFile, String outputFile, String freqFile) throws IOException {
		
		BinaryIn fileToCode = new BinaryIn(inputFile);        // File to encode
		BinaryOut fileOutput = new BinaryOut(outputFile);     // File to output encoded data

		String inputString = fileToCode.readString();         // Reads all input from fileToCode
		char[] inputChars = inputString.toCharArray();        // and puts it to a charArray
		
		int[] freqArray = buildFreqArray(inputChars);         // Creates the array of character frequencies
		
		Node root = buildHuffmanTrie(freqArray);              // Builds Huffman trie and holds on to root node
		
		String[] codeStrings = buildCode(root);               // Creates array of code strings for characters
		
		/* Takes each character of input and uses codeStrings
		 * to encode and output boolean values to encrypted file */
		for (int i = 0; i < inputChars.length; ++i) {
			String code = codeStrings[inputChars[i]];
			for (int j = 0; j < code.length(); ++j) {
				if (code.charAt(j) == '1') {
					fileOutput.write(true);
				}
				else {
					fileOutput.write(false);
				}
			}
		}
		
		// Prepare to write freqFile to be used for decryption
		File trieOutput = new File(freqFile);
		if (!trieOutput.exists()) {
			trieOutput.createNewFile();
		}
		FileWriter writer = new FileWriter(trieOutput);
		BufferedWriter trieWriter = new BufferedWriter(writer);
		
		// Creates freqFile
		writeTrie(root, freqArray, trieWriter);
		
		trieWriter.close();
		fileOutput.close();
		
    }

	// Decryption method that utilizes previously built helper methods
    public void decode(String inputFile, String outputFile, String freqFile) throws IOException {
		
    	// Prepares to read freqFile for decryption
    	File trieReading = new File(freqFile);
    	/*if (!trieReading.exists()) {
    		trieReading.createNewFile();
    	}*/
    	FileReader reader = new FileReader(trieReading);
    	BufferedReader trieReader = new BufferedReader(reader);

    	// Gets number of characters
    	int numCharacters = Integer.parseInt(trieReader.readLine());
    	
    	// Rebuilds Huffman trie and holds on to root node
    	Node root = readTrie(trieReader);

    	// Prepares to read encrypted input and write decoded output
    	BinaryIn fileInput = new BinaryIn(inputFile);
    	BinaryOut fileOutput = new BinaryOut(outputFile);

    	/* Follows trie with each boolean it
    	 * reads and writes the matching character
    	 * when it reaches the leaf node */
    	for (int i = 0; i < numCharacters; ++i) {
    		Node x = root;
    		while (!x.isLeaf()) {
    			if (fileInput.readBoolean()) {
    				x = x.right;
    			}
    			else {
    				x = x.left;
    			}
    		}
    		fileOutput.write(x.character);
    	}
    	
    	fileOutput.close();
    }

   public static void main(String[] args) {
	  
       Huffman huffman = new HuffmanSubmit(); // Creates object for encryption/decryption
       
       // Tries to encode file, printing an error message if it fails
       try {
    	   huffman.encode(args[0], "enc.txt", "freqFile.txt");
       }
       catch (Exception excpt) {
    	   System.out.println("A problem occurred with encoding. Try again with a different file or file name.");
       }
       
       // Tries to decode file, printing an error message if it fails
       try {
    	   huffman.decode("enc.txt", args[1], "freqFile.txt");
       }
       catch (Exception excpt) {
    	   System.out.println("A problem occurred with decoding. Try again with a different file or file name.");
       }
   }

}
