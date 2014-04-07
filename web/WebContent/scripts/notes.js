// Author: Patrick Mutchler
// This script manages the note taking system 

var notes = {};
var currentMethod = "";
var app = "test"; // TODO - make this work for independent apps

/**** Public interface ****/

// Displays notes for each callee of a given callsite
function showNotes(callsite, filePath, lineNum){
	$.getJSON(
		'/stamp/html/getCallees.jsp',
		{ chordSig: callsite, filePath: filePath, lineNum: lineNum },
		function (data){
			buildNotesPanel(data); 
		}
	);
}

// Opens the note editing panel for a given method
function editNotes(method){
	saveNotes();
	
	showElement("#notes_pane")
	hideElement("#notes_viewer")
	showElement("#notes_editor")
	
	text = getNotes(method);
	$("#editor").val(text);
	
	// need to save current method for when we save notes
	currentMethod = method; 
}

// Saves the notes in the note editing panel
function saveNotes(){
	if(currentMethod !== ""){
		notes[currentMethod] = $("#editor").val();
	}
}

/**** Private helper functions ****/

// Saves the current notes in local storage. Notes are saved with timestamps so
// they can be deleted after they expire.
function exportNotes(){
	saveNotes();
	
	notes_str = localStorage.getItem("stamp_notes");
	all_notes = notes_str !== null ? JSON.parse(notes_str) : {};

	all_notes[app] = {
		"notes": JSON.stringify(notes),
		"date": new Date().toString()
	};
	localStorage.setItem("stamp_notes", JSON.stringify(all_notes));
	console.log("Saved: " + JSON.stringify(all_notes))
}

// Retrieves saved notes from local storage.
function importNotes(){
	notes_str = localStorage.getItem("stamp_notes");
	all_notes = notes_str !== null ? JSON.parse(notes_str) : {};
	notes = all_notes[app] ? JSON.parse(all_notes[app]["notes"]) : {};
} 

function getNotes(method){
	if(notes[method])
		return notes[method];
	return "";
}

function clearStorage(){
	localStorage.removeItem("stamp_notes");
	notes = {}
}

/**** DOM management ****/

// This is super ugly, templates would be better
function buildNotesPanel(data){
	html = "<table class='table'><tbody>";
	
	for(var i = 0; i < data.length; i++){
		callee = data[i];
		note = getNotes(callee);
		html += noteTemplate(callee, note);
	}
	
	html += "</tbody></table>";
	
	$("#notes_viewer").html(html);
	showElement("#notes_pane");
	hideElement("#notes_editor");
	showElement("#notes_viewer");
}

function noteTemplate(callee, note){
	html = "<tr><td>"
	
	html += callee + "</br>";
	html += "<pre>" + note + "</pre>";
	html += "</td></tr>"
	return html;
}

function showElement(id){
	$(id).addClass("notes_show");
	$(id).removeClass("notes_hide");
}

function hideElement(id){
	$(id).addClass("notes_hide");
	$(id).removeClass("notes_show");
}

// Imports notes from local storage when the page is first loaded
// and exports notes to local storage when the page is closed.
$(document).ready(function() {
	window.onunload = exportNotes;
	importNotes();
});


