package com.holum.it.codes;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Implementation of the standard Huffman Coding algorithm.
 * 
 * http://en.wikipedia.org/wiki/Huffman_coding
 * 
 * @author eho
 *
 */
public class HuffmanCode {

    // Number of characters in our source alphabet (extended ascii)
    private static final int ASCII_SIZE = 256;
    
    // For encodings
    private static final char ZERO = '0';
    private static final char ONE = '1';

    private String plaintext;
    
    private String compressedBinary;
    private String compressedText;
    
    private Node root;
    
    private int[] frequencies;
    private String[] charEncodings;
    
    public String getPlaintext() {
        return plaintext;
    }

    public String getCompressedBinary() {
        return compressedBinary;
    }

    public String getCompressedText() {
        return compressedText;
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
    
    public void printTree() {
        
    }
    
    public String decode(String codetext, int[] frequencies) {
        Node root = buildTree(frequencies);
        
        Map<Character, String> codeFunction = new HashMap<>();
        constructCodeMap(root, "");
        
        return null;
    }
    
    public String encode(String plaintext) {
        this.plaintext = plaintext;
        this.frequencies = computeFrequencies(plaintext);
        this.root = buildTree(frequencies);
        
        this.charEncodings = new String[ASCII_SIZE];
        constructCodeMap(root, "");
        
        // Encodings
        this.compressedBinary = encodeBinary(plaintext, charEncodings);
        this.compressedText = encodeASCII(compressedBinary.toString());
        
        return compressedText;
    }
    
    private String encodeASCII(String binaryString) {
        StringBuffer newString = new StringBuffer();
        for (int i=0; i<Math.ceil(binaryString.length()/8); i++) {
            int start = i*8;
            int chunkSize = Math.min(8, binaryString.length() - start);
            String piece = binaryString.substring(start, start + chunkSize);
            newString.append((char) Integer.parseInt(piece, 2));
        }
        return newString.toString();
    }
    
    private String encodeBinary(String plaintext, String[] charEncodings) {
     // Encode string
        StringBuilder binaryString = new StringBuilder();
        for (int i=0; i<plaintext.length(); i++) {
            binaryString.append(charEncodings[plaintext.charAt(i)]);
        }
        return binaryString.toString();
    }
    
    private void constructCodeMap(Node n, String code) {
        n.encoding = code;
        
        if (n.isLeaf()) {
            charEncodings[n.sourceSymbol] = n.encoding;   
            return;
        }
        
        constructCodeMap(n.left, code + ZERO);
        constructCodeMap(n.right, code + ONE);
    }
    
    /**
     * Builds
     * @param frequencies
     * @return
     */
    private Node buildTree(int[] frequencies) {
        // To sort by frequency as we compute nodes
        PriorityQueue<Node> nodes = new PriorityQueue<>();
        
        for (int i=0; i<frequencies.length; i++) {
            if (frequencies[i] == 0) continue;
            Node n = new Node(frequencies[i], (char) i);
            nodes.add(n);
        }
        
        // Continuously remove, combine, and merge in the two smallest nodes, keeping them 
        // in the Queue to maintain size order.
        while (nodes.size() >= 2) {
            Node min_1 = nodes.poll();
            Node min_2 = nodes.poll();
            nodes.remove(min_2);
            
            Node combo = new Node(min_1.weight + min_2.weight, (char) -1);
            combo.left = min_2;
            combo.right = min_1;
            
            nodes.add(combo);
        }
        
        return nodes.poll(); // should have p ~= 1
    }
    
    /**
     * Returns a count of character frequencies in the plaintext.
     */
    private int[] computeFrequencies(String plaintext) {
        int[] freqs = new int[ASCII_SIZE];
        
        for (int i=0; i<plaintext.length(); i++) {
            freqs[plaintext.charAt(i)]++;
        }
        
        return freqs;
    }
    
    /** 
     * Node of the Huffman Tree.
     */
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
            return (left == null) && (right == null);
        }
        
        @Override
        public String toString() {
            return String.format("Symbol=%s / Probability=%s / Encoding=%s", sourceSymbol, weight, encoding);
        }
    }
}
