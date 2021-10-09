Huffman Encoding Project README

Grayson Westfall
email: graywestfall@gmail.com

The project takes two command line arguments (which should be files: they can both be text files, both jpgs, etc.), encodes the first command line argument file into a file named enc.txt, then decodes enc.txt and deposits the decoded result into a file by the name of the second command line argument.
This project contains four files:

	- BinaryIn.java		(borrowed class from Robert Sedgewick and Kevin Wayne from Algorithms, 4th Edition [documentation in file])
	- BinaryOut.java		(borrowed class from Robert Sedgewick and Kevin Wayne from Algorithms, 4th Edition [documentation in file])
	- Huffman.java		(interface that contains the required 'encode' and 'decode' methods as used in HuffmanSubmit.java)
	- HuffmanSubmit.java	(main class that actually implements both the 'encode' and 'decode' methods for the encoding/decoding and includes supplementary classes/methods to help with this process)

Essentially all my work is contained in the HuffmanSubmit.java, so I will primarily discuss what I created within that file.

I began by defining a Node class that would hold frequency values / characters as well as left/right children. These Nodes would comprise the trie used for encoding later on. Next, I created a MinPriorityQueue class (as well as all its helper/instance methods) which would be used to construct the actual HuffmanTrie (in other words, the best possible coding based on character frequencies). I then created helper methods which would be used in the encode() method.  

They are as follows:
	- buildFreqArray(): builds and returns the array of character 
			   frequencies in the file to be encoded

	- buildHuffmanTrie(): builds the HuffmanTrie by using the MinPriorityQueue, 
			     and returns the root node of the trie

	- buildCode() (non-recursive): the normal buildCode() method is simply a 
				     method that calls the recursive buildCode() 
				     method and returns the array of codeStrings it 
				     created.

	- buildCode() (recursive): the recursive buildCode() method builds the coded 
				  string for each character and

	- writeTrie() method: writes the frequency file that would be used to decode a 
			     previously encoded file

	- readTrie() method: reads the frequency file created from encoding a previous 
			    file and rebuilds the HuffmanTrie using the frequency 
			    file's information, again returning the root node


Using these helper methods, the encode() method follows this process:

	1. Reads the input file and saves its contents as a character array
	2. Calls buildFreqArray() and keeps a reference to the array
	3. Calls buildHuffmanTrie() and keeps a reference to the root node
	4. Calls buildCode() and keeps a reference to the code strings array
	5. Uses the code strings to code each individual character of the input
	6. Calls writeTrie()

The decode() method follows this process:

	1. Takes the number of characters in the encoded file from the frequency file
	2. Calls readTrie()
	3. Follows the trie by reading booleans until reaching a character
	4. Writes the character to the decrypted file
	5. Continues until the entire encoded file is decrypted

