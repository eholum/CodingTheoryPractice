package com.holum.it.codes;

import java.util.PriorityQueue;

/**
 * Implementation of the standard Huffman Coding algorithm. The plain and compressed texts
 * are treated as an object, rather than a function - so as to be able to have each
 * piece of the process available at once.
 * 
 * http://en.wikipedia.org/wiki/Huffman_coding
 *
 */
public class Huffman {

    // For encodings
    private static final char ZERO = '0';
    private static final char ONE = '1';

    private String plaintext; // uncompressed text
    private String compressedBinary; // compressed text in binary code alphabet
    private Node root; // root of the Huffman tree
    private int[] frequencies; // character frequencies table
    private String[] charEncodings; // character encodings  
    
    // Number of characters in our source alphabet (extended ascii)
    private static final int ASCII_SIZE = 256;
    
    public String getPlaintext() {
        return plaintext;
    }

    public String getCompressedBinary() {
        return compressedBinary;
    }
    
    public int[] getFrequencies() {
        return frequencies;
    }
    
    public void setFrequencies(int[] frequencies) {
        this.frequencies = frequencies;
    }

    public String[] getCharEncodings() {
        return charEncodings;
    }
    
    /**
     * Create a Huffman object from a given plaintext.
     */
    public Huffman(String plaintext) {
        this.plaintext = plaintext;
        this.compressedBinary = compress(plaintext);
    }
    
    private String compress(String plaintext) {
        this.frequencies = computeFrequencies(plaintext);
        this.root = buildTree(frequencies);
        
        this.charEncodings = new String[ASCII_SIZE];
        constructCodeMap(root, "");
        
        return encodeBinary(plaintext, charEncodings);
    }
    
    // Maps characters to their Huffman encodings using the computed mappings.
    private String encodeBinary(String plaintext, String[] charEncodings) {
        StringBuilder binaryString = new StringBuilder();
        for (int i=0; i<plaintext.length(); i++) {
            binaryString.append(charEncodings[plaintext.charAt(i)]);
        }
        return binaryString.toString();
    }
    
    // Returns a count of character frequencies in the given text.
    private int[] computeFrequencies(String plaintext) {
        int[] freqs = new int[ASCII_SIZE];
        
        for (int i=0; i<plaintext.length(); i++) {
            freqs[plaintext.charAt(i)]++;
        }
        
        return freqs;
    }
    
    /**
     * Create a Huffman object from compressed text and frequency count.
     */
    public Huffman(String compressedBinary, int[] frequencies) {
        this.compressedBinary = compressedBinary;
        this.frequencies = frequencies;
        this.plaintext = expand(compressedBinary, frequencies);
    }
    
    private String expand(String compressedBinary, int[] frequencies) {
        this.frequencies = frequencies;
        this.root = buildTree(frequencies);
        
        this.charEncodings = new String[ASCII_SIZE];
        constructCodeMap(root, "");
        
        return decodeBinary(compressedBinary);
    }
    
    private String decodeBinary(String compressedBinary) {
        StringBuffer plaintext = new StringBuffer();
        Node it = root;
        for (int i=0; i<compressedBinary.length(); i++) {
            it = compressedBinary.charAt(i) == ZERO ? it.left : it.right;
            if (it.isLeaf()) {
                plaintext.append(it.sourceSymbol);
                it = root;
            }
        }
        
        return plaintext.toString();
    }
    
    // Recursively fills in char encodings
    private void constructCodeMap(Node n, String code) {
        n.encoding = code;
        if (n.isLeaf()) {
            charEncodings[n.sourceSymbol] = n.encoding;   
            return;
        }
        constructCodeMap(n.left, code + ZERO);
        constructCodeMap(n.right, code + ONE);
    }
    
    // Constructs the Huffman tree using a priority queue.
    private Node buildTree(int[] frequencies) {
        // To sort by frequency as we compute nodes
        PriorityQueue<Node> nodes = new PriorityQueue<>();
        
        for (int i=0; i<frequencies.length; i++) {
            if (frequencies[i] == 0) continue;
            Node n = new Node(frequencies[i], (char) i);
            nodes.add(n);
        }
        
        // Continuously remove, combine, and merge in the two smallest nodes, keeping them 
        // in the queue to maintain size order.
        while (nodes.size() >= 2) {
            Node min_1 = nodes.poll();
            Node min_2 = nodes.poll();
            nodes.remove(min_2);
            
            Node combo = new Node(min_1.weight + min_2.weight, (char) 78);
            combo.left = min_2;
            combo.right = min_1;
            
            nodes.add(combo);
        }
        
        return nodes.poll(); // return the root.
    }
    
    // For building the Huffman tree.
    private static class Node implements Comparable<Node> {
        
        public final int weight; // probability of source symbol
        public final char sourceSymbol; // source symbol, will be -1 for internal nodes. ie. non-leaf nodes.
        public String encoding;
        
        public Node left;
        public Node right;
        
        public Node(int w, char c) {
            this.weight = w;
            this.sourceSymbol = c;
            encoding = "";
        }

        public int compareTo(Node n) {
            return this.weight - n.weight;
        }
        
        public boolean isLeaf() {
            // Given the construction of the tree, we can omit individual checks.
            return (left == null) && (right == null);
        }
    }
}
