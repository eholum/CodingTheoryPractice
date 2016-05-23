"""
Module for compressing and expanding huffman codes for raw text files.

Usage:

python Huffman.py -h
"""
from argparse import ArgumentParser
from collections import defaultdict
from heapq import heapify, heappop, heappush

class Node:
    """
    A node in a Huffman tree.
    """
    def __init__(self, char=None, left=None, right=None):
        self.char = char
        self.left = left
        self.right = right

def compute_frequencies(infile):
    """
    Computes character frequency count in a file, returns 
    (total file length, default dictionary for character count)
    """
    frequencies = defaultdict(int)
    with open(infile, 'r') as fi:
        for c in fi.read():
            frequencies[c] += 1
    return frequencies    

def build_tree(freqs):
    """
    Builds the huffman tree according to the dictionary of character frequency
    information.
    """
    heap = [[weight, Node(char)] for char, weight in freqs.iteritems()]
    heapify(heap)
    while (len(heap) > 1):
        low = heappop(heap)
        high = heappop(heap)
        heappush(heap, [low[0] + high[0], Node(None, low[1], high[1])])
    return heappop(heap)[1]


def walk_tree(node, dictionary, code=''):
    """
    Fills in huffman tree with encoding information.
    """
    if not node.left and not node.right:
        dictionary[node.char] = code
        node.code = code
        return
    walk_tree(node.left, dictionary, code + '0')
    walk_tree(node.right, dictionary, code + '1')
    
    
def create_header(freqs):
    """
    Simple header recording character frequency counts. There's probably a more efficient
    and better way to do this...
    """
    return '/'.join(['{0}/{1}'.format(ord(ch), freqs[ch]) for ch in freqs]) + '\n'


def write_compress(infile, outfile, dic, header):
    """
    Writes encoded data to specified outfile, adds frequency count as header.
    """
    # TODO: This is horribly inefficient
    with open(infile, 'r') as fi:
        data = ''.join([dic[c] for line in fi for c in line])
    
    length, remainder = divmod(len(data), 8)
    bytes = [int(s,2) for s in [data[i << 3:(i + 1) << 3] for i in xrange(length)]]
    
    # If there are any bits leftover we fill in the last byte with 0s.
    if remainder > 0:
        extra = data[-remainder:].ljust(8, '0')
        bytes.append(int(extra, 2))
    bytes.append(8 - remainder) # Last byte records remainder.

    with open(outfile, 'wb') as out:
        out.write(header)
        out.write(pack('%dB' % len(bytes), *bytes))
    return data
    
    
def compress(infile, outfile):
    """
    Compresses inputted text according to Huffman's algorithm.
    """
    freqs = compute_frequencies(infile)
    root = build_tree(freqs)
    dictionary = {}
    walk_tree(root, dictionary)
    header = create_header(freqs)
    data = write_compress(infile, outfile, dictionary, header)


def parse_header(header):
    """
    Returns a default dict populated with the header values in the huffman file.
    """
    frequencies = defaultdict(int)
    values = header.split('/')
    for i in xrange(0, len(values), 2):
        frequencies[chr(int(values[i]))] = float(values[i+1])
    return frequencies


def write_expand(data, fi, root):
    """
    Expands a file.
    """
    node = root
    for bit in data:
        node = node.left if bit == '0' else node.right
        
        if node.left is None:
            fi.write(node.char)
            node = root

    
def expand(infile, outfile):
    """
    Expands a compressed huffman file.
    """
    with open(infile, 'rb') as fi: 
        header = fi.readline()
        raw_data = ''.join(fi.readlines())
    freqs = parse_header(header)
    root = build_tree(freqs)
    data = to_binary(raw_data)
    
    with open(outfile, 'w') as fi:
        write_expand(data, fi, root)
    

if __name__ == "__main__":
    parser = ArgumentParser(description="Compress text using the Huffman coding algorithm.")
    parser.add_argument("filename", help="Input file. Must end in .huffman if expanding file", type=str)
    parser.add_argument("-e", "--expand", help="Expand compressed file.", action="store_true")
    parser.add_argument("-o", "--output", help="Specify output directory. \
        otherwise default names will be used.")
    args = parser.parse_args()
    
    infile = args.filename
    if (args.expand):
        outfile = infile[:infile.rfind('.')] if args.output == None else args.output
        expand(infile, outfile)
    else:
        outfile = infile + '.huffman' if args.output == None else args.output
        compress(infile, outfile)