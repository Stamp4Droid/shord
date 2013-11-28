import errno
from itertools import izip, count
import os
import re
import string
import xml.etree.ElementTree as ET

class FinalAttrs(object):
    """
    A class that enforces a single assignment to any instance attribute.

    Any object of this class (or any subclass of it) is prohibited from
    assigning to the same attribute twice. If this happens, an exception is
    raised.

    "Private" attributes, i.e. those whose name begins with an underscore, are
    excluded from this constraint (these should only be manipulated within the
    class code anyway). Any attributes found in `self._mutables` (which should
    contain a list of attribute names) are also excluded.

    To make use of this class, have your class inherit from it instead of
    @e object, and make sure you initialize all public attributes in the
    constructor. You can whitelist a custom set of mutable attributes by
    storing their names (as strings) in `self._mutables`.
    """

    # TODO: Should instead check the calling function (read the top of the
    # stack).

    def __init__(self):
        raise NotImplementedError() # abstract class

    def __setattr__(self, name, value):
        if (not hasattr(self, name) or name.startswith('_') or
            hasattr(self, '_mutables') and name in self._mutables):
            super(FinalAttrs, self).__setattr__(name, value)
        else:
            raise Exception('Attribute %s is final' % name)

class Hashable(FinalAttrs):
    def __init__(self):
        raise NotImplementedError() # abstract class

    def __key__(self):
        raise NotImplementedError()

    def __eq__(self, other):
        return type(other) == type(self) and self.__key__() == other.__key__()

    def __hash__(self):
        return hash(self.__key__())

class MultiDict(FinalAttrs):
    """
    An append-only dictionary, where a single key can be associated with more
    than one value. By default, a key is mapped to nothing.
    This is an abstract base class, that doesn't specify the container to use
    for storing the values associated with each key. Concrete subclasses should
    define that container, by implementing MultiDict#empty_container() and
    MultiDict#append_to_container().
    """

    def __init__(self):
        """
        Create an empty dictionary (where every key maps to no value).
        """
        self._dict = {}

    def append(self, key, value):
        """
        Add a value to the container associated with some key.
        """
        if not key in self._dict:
            self._dict[key] = self.empty_container()
        self.append_to_container(self._dict[key], value)

    def get(self, key):
        """
        Get all the values associated with some key.
        """
        return self._dict.get(key, self.empty_container())

    def __iter__(self):
        """
        Iterate over those keys that map to at least one value.
        """
        for k in self._dict:
            yield k

    def __str__(self):
        return '\n'.join(['\n'.join(['%s:' % k] +
                                    ['\t%s' % v for v in self._dict[k]])
                          for k in self._dict])

    def empty_container(self):
        """
        Create a new empty container of the kind used for storing dictionary
        values (abstract method).
        """
        raise NotImplementedError()

    def append_to_container(self, container, value):
        """
        Append a value to a container of the kind used for storing dictionary
        values (abstract method).
        """
        raise NotImplementedError()

class OrderedMultiDict(MultiDict):
    """
    A MultiDict variant where the values associated with each key are ordered
    by insertion time.

    The underlying implementation uses a simple list as the container for
    values.
    """

    def __init__(self):
        super(OrderedMultiDict, self).__init__()

    def empty_container(self):
        return []

    def append_to_container(self, list, value):
        list.append(value)

class UniqueMultiDict(MultiDict):
    """
    A MultiDict variant where all values associated with a specific key are
    distinct.

    The underlying implementation uses a set as the container for values.
    """

    def __init__(self):
        super(UniqueMultiDict, self).__init__()

    def empty_container(self):
        return set()

    def append_to_container(self, set, value):
        set.add(value)

class UniqueNameMap(FinalAttrs):
    """
    An object used to manage all instances of a given class. An instance of
    this class is expected to be the single point of creation for all objects
    of its managed class (the rest of the code shouldn't construct such objects
    in any other way). Each element object managed by this class will have a
    unique name, assigned by the client code. Each element will also get a
    unique reference number, allocated by the manager.

    This is an abstract base class, that doesn't specify the class of element
    objects. Concrete subclasses should define that class, by implementing
    UniqueNameMap#managed_class(). Concrete subclasses can also specify rules
    for allowed names, by overriding UniqueNameMap#valid_name().
    """

    def __init__(self):
        self._list = []
        self._dict = {}

    def _make(self, name, *params):
        """
        Construct a new object, to add to the map or verify its equality with
        an existing one of the same name.
        """
        args = [name, self.size()] + list(params)
        elem = (self.managed_class())(*args)
        return elem

    def _add(self, name, elem):
        # TODO: Must ensure we haven't added any other elements since elem's
        # construction, so that the reference passed to it is still valid.
        self._list.append(elem)
        self._dict[name] = elem

    def find(self, name):
        """
        Return the element with the specified @a name, if it exists, otherwise
        return @e None.
        """
        return self._dict.get(name)

    def get(self, name, *props):
        """
        Return the element with the specified @a name and properties. Create
        that element if it doesn't already exist.
        """
        assert self.valid_name(name), "Invalid name: %s" % name
        new = self._make(name, *props)
        existing = self.find(name)
        if existing is not None:
            assert existing == new
            return existing
        else:
            self._add(name, new)
            return new

    def __iter__(self):
        """
        Iterate over all elements added so far, sorted by reference number.
        """
        for s in self._list:
            yield s

    def ref2elem(self, ref):
        """
        Get the element corresponding to some reference number.
        """
        return self._list[ref]

    def size(self):
        """
        Get the number of elements stored so far.
        """
        return len(self._list)

    def managed_class(self):
        """
        Return the class of elements stored in this map (abstract method).

        The first two parameters of this class's constructor must correspond to
        the new object's name and reference number (the constructor is free to
        ignore these), which will be supplied by the manager. The rest of the
        constructor's parameters should be supplied by the client when calling
        UniqueNameMap#get(). The managed class should also define a non-trivial
        equality operator.
        """
        raise NotImplementedError()

    def valid_name(self, name):
        """
        Check that @a name is a valid name for elements stored in this map
        (overridable method).
        """
        return True

class CodePrinter(FinalAttrs):
    """
    A helper class for pretty-printing C code.
    """
    # TODO: Should rename functions to 'emit'.

    def __init__(self, out):
        """
        @param [in] out A file-like object to print to.
        """
        self._out = out
        self._level = 0

    def write(self, line, newline=True):
        if line.startswith('}'):
            self._level -= 1
        is_case_start = line.startswith('case') or line.startswith('default')
        real_level = self._level - (1 if is_case_start else 0)
        self._out.write('  ' * real_level + line + ('\n' if newline else ''))
        if line.endswith('{'):
            self._level += 1

def to_c_bool(py_bool):
    return 'true' if py_bool else 'false'

def to_py_bool(xml_bool):
    if xml_bool == 'true':
        return True
    elif xml_bool == 'false':
        return False
    else:
        assert False

def all_same(elems):
    if elems == []:
        return True
    for e in elems[1:]:
        if e != elems[0]:
            return False
    return True

def all_different(elems):
    return len(elems) == len(set(elems))


def idx2char(idx):
    assert idx >= 0 and idx < 26
    return chr(ord('a') + idx)

def enum(*sequential, **named):
    enums = dict(zip(sequential, range(len(sequential))), **named)
    return type('Enum', (), enums)

def switch_dir(src_file, tgt_dir, new_ext):
    base = os.path.basename(os.path.splitext(src_file)[0])
    return os.path.join(tgt_dir, base + '.' + new_ext)

def mkdir(path):
    try:
        os.mkdir(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise

class DomMap(FinalAttrs):
    def __init__(self, map_file):
        self._idx2val = []
        self._val2idx = OrderedMultiDict()
        for (idx, line) in izip(count(), map_file):
            val = line[:-1]
            self._idx2val.append(val)
            # XXX: It turns out, "unique" strings aren't always so
            # assert val not in self._val2idx
            self._val2idx.append(val, idx)

    def idx2val(self, idx):
        return self._idx2val[idx]

    def val2idx(self, val):
        return self._val2idx.get(val)

    def __iter__(self):
        for val in self._idx2val:
            yield val

class Edge(Hashable):
    def __init__(self, symbol, src, dst, index):
        self.symbol = symbol
        self.src = src
        self.dst = dst
        self.index = index

    def __key__(self):
        return (self.symbol, self.src, self.dst, self.index)

    def to_tuple(self):
        return ('%s %s%s' %
                (self.src, self.dst,
                 '' if self.index is None else (' %s' % self.index)))

    def to_file_base(self):
        index_str = '' if self.index is None else ('[%s]' % self.index)
        return ('%s%s.%s-%s' % (self.symbol, index_str, self.src, self.dst))

    def is_terminal(self):
        return self.symbol[0] in string.ascii_lowercase

    @staticmethod
    def from_tuple(symbol, str):
        m = re.match(r'^(\w+) (\w+)(?: ([0-9]+))?$', str)
        return Edge(symbol, m.group(1), m.group(2), m.group(3))

    @staticmethod
    def from_path_node(node):
        src = node.attrib['from']
        dst = node.attrib['to']
        index = node.get('index')
        if node.tag == 'NTStep' or node.tag == 'TempStep':
            edge = Edge(node.attrib['symbol'], src, dst, index)
            assert not edge.is_terminal()
        else:
            assert list(node) == []
            edge = Edge(node.tag, src, dst, index)
            assert edge.is_terminal()
        return edge

    @staticmethod
    def from_file_base(base):
        m = re.match(r'(\w+)(?:\[([0-9]+)\])?\.(\w+)-(\w+)', base)
        return Edge(m.group(1), m.group(3), m.group(4), m.group(2))

class Step(FinalAttrs):
    def __init__(self, reverse, edge):
        assert edge.is_terminal()
        self.reverse = reverse
        self.edge = edge

    def __str__(self):
        return ('%s %s %s %s%s'
                % ('REV' if self.reverse else 'STR', self.edge.symbol,
                   self.edge.src, self.edge.dst,
                   '' if self.edge.index is None else (' ' + self.edge.index)))

    @staticmethod
    def from_string(str):
        step_pat = r'^(STR|REV) ([a-z]\w*) (\w+) (\w+)(?: ([0-9]+))?$'
        m = re.match(step_pat, str)
        assert m is not None
        edge = Edge(m.group(2), m.group(3), m.group(4), m.group(5))
        return Step(m.group(1) == 'REV', edge)

class PathTree(FinalAttrs):
    def __init__(self, path_xml_file):
        root_node = ET.parse(path_xml_file).getroot()
        assert root_node.tag == 'path'
        assert len(list(root_node)) == 1
        self._top_node = root_node[0]
        assert self._top_node.tag == 'NTStep'

    def top_edge(self):
        return Edge.from_path_node(self._top_node)

    def walk(self, handle):
        def process(node, reverse):
            reverse = reverse ^ (node.attrib['reverse'] == 'true')
            edge = Edge.from_path_node(node)
            if edge.is_terminal():
                handle(Step(reverse, edge))
            else:
                for child in (reversed(node) if reverse else node):
                    process(child, reverse)
        process(self._top_node, False)
