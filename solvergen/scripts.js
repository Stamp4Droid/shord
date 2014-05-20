var names = {};

function recordName(mnem, name) {
    names[mnem] = name;
}

// ============================================================================

function index(map, key, def) {
    if (!(key in map)) {
	map[key] = def;
    }
    return map[key];
}

function count(obj) {
    if (!obj) {
	return 0;
    }
    if (obj instanceof Array) {
	return obj.length;
    }
    var c = 0;
    for (var key in obj) {
	c += count(obj[key]);
    }
    return c;
}

// ============================================================================

var lastLvl = 3;
var selection = [];
var newColors = ['pink', 'pink', 'lightcyan', 'pink'];
var prevColors = [];

function setColor(elem, color) {
    var aWrapper = elem.childNodes[2];
    var prev;
    if (elem.className.baseVal === 'node') {
	prev = aWrapper.childNodes[1].style.fill;
	aWrapper.childNodes[1].style.fill = color;
    } else {
	prev = aWrapper.childNodes[1].style.stroke;
	aWrapper.childNodes[1].style.stroke = color;
	aWrapper.childNodes[3].style.stroke = color;
	aWrapper.childNodes[3].style.fill = color;
    }
    return prev;
}

function unpick(upFrom) {
    for (var i = lastLvl; i >= upFrom; i--) {
	if (selection[i]) {
	    setColor(selection[i], prevColors[i]);
	    selection[i] = undefined;
	    prevColors[i] = undefined;
	    updateAnnots(i);
	}
    }
}

function pick(lvl, elem) {
    for (var i = 0; i < lvl; i++) {
	if (!selection[i]) {
	    return;
	}
    }
    var prev = selection[lvl];
    unpick(lvl);
    if (elem !== prev) {
	selection[lvl] = elem;
	prevColors[lvl] = setColor(elem, newColors[lvl]);
	updateAnnots(lvl);
    }
    updateEffect();
}

function pickNode(node) {
    var toks = node.id.split('::');
    pick(+(toks[0]), node);
    var name = names[toks[1]];
    if (name) {
	document.getElementById('name').textContent = name;
    }
}

function isSummary(edge) {
    return edge.id.indexOf('=>') > -1;
}

function pickEdge(edge) {
    if (isSummary(edge)) {
	pick(0, edge);
    } else {
	document.getElementById('labels').textContent =
	    edge.childNodes[2].attributes['xlink:title'].value;
    }
}

// ============================================================================

var effects = {};

function recordEffect(dst, priTo, secFrom, secTo, stackEfft) {
    index(index(index(index(effects,
			    dst, {}),
		      priTo, {}),
		secFrom, {}),
	  secTo, []).push(stackEfft);
}

function selectEffects(lvl) {
    if (lvl >= 0 && !selection[lvl]) {
	return undefined;
    }
    var map = effects;
    for (var i = 0; i <= lvl; i++) {
	map = map[selection[i].id.split('::')[1]];
	if (!map) {
	    break;
	}
    }
    return map;
}

function updateEffect() {
    var efftStr = '';
    var effts = selectEffects(lastLvl);
    if (effts) {
	efftStr = effts.join('\n');
    }
    document.getElementById('effect').textContent = efftStr;
}

function updateAnnots(after) {
    if (after < lastLvl) {
	annotateNodes((after + 1).toString(), selectEffects(after));
    }
}

function annotateNodes(prefix, map) {
    var svgs = document.getElementsByTagName('svg');
    for (var i = 0; i < svgs.length; i++) {
	var graph = svgs[i].childNodes[3];
	if (graph.id.split('::')[0] !== prefix) {
	    continue;
	}
	var nodes = graph.childNodes;
	for (var j = 0; j < nodes.length; j++) {
	    if (nodes[j].tagName !== 'g' ||
		nodes[j].className.baseVal !== 'node') {
		continue;
	    }
	    var textNode = nodes[j].childNodes[2].lastChild.previousSibling;
	    if (textNode.tagName !== 'text') {
		continue;
	    }
	    var c = '';
	    if (map) {
		c = count(map[nodes[j].id.split('::')[1]]).toString();
	    }
	    var annotNode = textNode.lastChild;
	    if (annotNode.tagName !== 'tspan') {
		annotNode =
		    document.createElementNS('http://www.w3.org/2000/svg',
					     'tspan');
		annotNode.setAttribute('fill', 'red');
		annotNode.setAttribute('baseline-shift', 'super');
		textNode.appendChild(annotNode);
	    }
	    annotNode.textContent = c;
	}
    }
}

// ============================================================================

function parentG(elem) {
    while (elem.tagName !== 'g') {
	elem = elem.parentNode;
    }
    return elem;
}

window.onload = function() {
    var nodes = document.getElementsByClassName('node');
    for (var i = 0; i < nodes.length; i++) {
	nodes[i].addEventListener('click', function(evt) {
	    pickNode(parentG(evt.target));
	});
    }
    var edges = document.getElementsByClassName('edge');
    for (var i = 0; i < edges.length; i++) {
	edges[i].addEventListener('click', function(evt) {
	    pickEdge(parentG(evt.target));
	});
    }
    updateAnnots(-1);
}
