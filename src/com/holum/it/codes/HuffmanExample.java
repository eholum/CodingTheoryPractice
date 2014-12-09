package com.holum.it.codes;

public class HuffmanExample {
    
    public static void main(String[] args) {
        String plaintext = "the quick brown fox jumped over the lazy dog!";
        
        Huffman encoded = new Huffman(plaintext);
        System.out.println(encoded.getCompressedBinary());
        String[] encodings = encoded.getCharEncodings();
        for (int i=0; i<encodings.length; i++) {
            if (encodings[i] != null) {
                System.out.println((char) i + "=" + encodings[i]);
            }
        }
        
        Huffman decoded = new Huffman(encoded.getCompressedBinary(), encoded.getFrequencies());
        System.out.println(decoded.getPlaintext());
    }
}
