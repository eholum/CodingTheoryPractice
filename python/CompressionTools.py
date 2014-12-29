from collections import defaultdict
from struct import pack, unpack

import math  

# TODO: Make not suck
def create_header(freqs):
    """
    Simple header recording character frequency counts. There's probably a more efficient
    and better way to do this.
    """
    return '/'.join(['{0}/{1}'.format(ord(ch), freqs[ch]) for ch in freqs]) + '\n'


def parse_header(header):
    """
    Returns a default dict populated with the header values in the huffman file.
    """
    frequencies = defaultdict(int)
    values = header.split('/')
    for i in xrange(0, len(values), 2):
        frequencies[chr(int(values[i]))] = float(values[i+1])
    return frequencies


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


def to_binary(raw_data):
    """
    Converts packed data to a long string of 0s and 1s.
    """
    bytes = [unpack('%dB' % len(rd), rd)[0] for rd in raw_data]
    
    data = ''.join([bin(int(byte))[2:].rjust(8, '0') for byte in bytes])
    
    # Remove last byte and trailing 0s.
    return data[:-8 - int(data[-8:], 2)]


def entropy(probs, r):
    """
    Computes the entropy for a random variable distribution.
    """
    return sum(p*math.log(1/p, r) for p in probs)