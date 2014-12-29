"""
Module for compressing and expanding text files using the Shannon
Fano Elias coding method.

python ShannoFano.py --help
"""
from argparse import ArgumentParser
from collections import defaultdict

import CompressionTools
import math
 
def float_to_binary(f_bar, bits):
    """
    Converts a float to its binary representation with bits number of digits.
    """
    binary = []
    for i in xrange(1, bits + 1):
        m = 1/math.pow(2, i)
        binary.append(str(int(f_bar / m)))
        f_bar = f_bar % m
    return "".join(binary)
        

def to_probs(frequencies):
    """
    Converts character frequency count to a probability distribution.
    """
    filelength = float(sum(frequencies.values()))
    probabilities = frequencies.copy()
    for ch in frequencies:
        probabilities[ch] = frequencies[ch]/filelength
    return probabilities
    

def construct_dictionary(frequencies):
    """
    Uses the frequency list to construct the character-code word dictionary.
    Computes \sum(p(a)) + 1/2 p(x) for all x in frequencies and a < x as well as
    the correct number of bits to use in the encoding for ch.
    """
    probabilities = to_probs(frequencies)
    dictionary = {}
    f = 0.0
    for ch in probabilities:
        bits = int(math.ceil(math.log(1/probabilities[ch], 2))) + 1
        f_bar = f + (.5 * probabilities[ch])
        dictionary[ch] = float_to_binary(f_bar, bits)
        f += probabilities[ch]
    return dictionary


def compress(infile, outfile):
    """
    Compresses inputted text according to the Shannon-Fano algorithm.
    """
    frequencies = CompressionTools.compute_frequencies(infile)
    dictionary = construct_dictionary(frequencies)
    header = CompressionTools.create_header(frequencies)
    CompressionTools.write_compress(infile, outfile, dictionary, header)
    

def write_expansion(binary, dictionary, fi):
    """
    Decodes bit by bit, and writes to outfile.
    """
    code_dictionary = {}
    for ch in dictionary: code_dictionary[dictionary[ch]] = ch
    
    s = ''
    for bit in binary:
        s += bit
        if s in code_dictionary:
            fi.write(code_dictionary[s])
            s = ''
    
    
def expand(infile, outfile):
    """
    Expands a compressed shannon-fano file.
    """
    with open(infile, 'rb') as fi: 
        header = fi.readline()
        raw_data = ''.join(fi.readlines())
    
    dictionary = construct_dictionary(CompressionTools.parse_header(header))
    binary = CompressionTools.to_binary(raw_data)
    
    with open(outfile, 'w') as fi:
        write_expansion(binary, dictionary, fi)


if __name__ == "__main__":
    parser = ArgumentParser(description="Compress text using the Shannon-Fano coding algorithm.")
    parser.add_argument("filename", help="Input file. Must end in .shan if expanding file", type=str)
    parser.add_argument("-e", "--expand", help="Expand compressed file.", action="store_true")
    parser.add_argument("-o", "--output", help="Specify output directory. \
        otherwise default names will be used.")
    args = parser.parse_args()
    
    infile = args.filename
    if (args.expand):
        outfile = infile[:infile.rfind('.')] if args.output == None else args.output
        expand(infile, outfile)
    else:
        outfile = infile + '.shan' if args.output == None else args.output
        compress(infile, outfile)