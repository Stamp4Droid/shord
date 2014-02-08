package stamp.missingmodels.grammars;
import stamp.missingmodels.util.jcflsolver.*;

/* Original Grammar:
###################
# CONFIGURATION
###################

.weights assignArg 1
.weights assignRet 1

###################
# INPUTS
###################

# src and sink annotations: src2Label, sink2Label
# label annotations: label2RefT, label2PrimT
# sinkf annotations: sink2RefT, sink2PrimT, sinkF2RefF, sinkF2PrimF
# transfer annotations: ref2RefT, ref2PrimT, prim2RefT, prim2PrimT 
# flow annotations: ref2RefF, ref2PrimF, prim2RefF, prim2PrimF

# pt: pt, fptArr
# field: fpt
# helper: assignPrimCtxt, assignPrimCCtxt, loadPrimCtxtArr, storePrimCtxtArr
# field helper: loadPrimCtxt, loadStatPrimCtxt, storePrimCtxt, storeStatPrimCtxt

###################
# RULES: POINTER ANALYSIS
###################

Assign :: assignCtxt
Assign :: assignArgCtxt
Assign :: assignRetCtxt

Pt :: _newCtxt
Pt :: _Assign Pt

Fpt[f] :: _Pt storeCtxt[f] Pt
Pt :: _loadCtxt[f] Pt _Fpt[f]

FptStat[f] :: _Pt storeStatCtxt[f] Pt
Pt :: _loadStatCtxt[f] Pt _FptStat[f]

# storeArr :: store[0]
FptArr :: _Pt storeArr Pt

###################
# RULES: ANNOTATION CONVERSION
###################

# transfer annotations

Label2RefT :: label2RefT
Label2PrimT :: label2PrimT

# flow annotations

SinkF2RefF :: sinkF2RefF
SinkF2PrimF :: sinkF2PrimF

Ref2RefF :: ref2RefF
Ref2PrimF :: ref2PrimF
Prim2RefF :: prim2RefF
Prim2PrimF :: prim2PrimF

###################
# RULES: ANNOTATION CONVERSION
###################

Ref2RefT :: ref2RefT
Ref2PrimT :: ref2PrimT
Prim2RefT :: prim2RefT
Prim2PrimT :: prim2PrimT

###################
# RULES: OBJECT ANNOTATIONS
###################

Obj2RefT :: _Pt Ref2RefT
Obj2PrimT :: _Pt Ref2PrimT
Obj2RefT :: _FptArr Obj2RefT
Obj2PrimT :: _FptArr Obj2PrimT

Label2ObjT :: Label2RefT Pt
Label2ObjT :: Label2ObjT Fpt[*]

###################
# RULES: SINKF
###################

# Sink_full-obj flow

SinkF2Obj :: SinkF2RefF Pt
SinkF2Obj :: sink2Label Label2Obj _Pt _Ref2RefF Pt
SinkF2Obj :: sink2Label Label2Prim _Ref2PrimF Pt
SinkF2Obj :: SinkF2Obj Fpt[*]

# Sink_full-prim flow

SinkF2Prim :: SinkF2PrimF
SinkF2Prim :: sink2Label Label2Obj _Pt _Prim2RefF
SinkF2Prim :: sink2Label Label2Prim _Prim2PrimF

###################
# RULES: SRC-SINK FLOW
###################

Src2Sink :: src2Label Label2Obj _SinkF2Obj
Src2Sink :: src2Label Label2Prim _SinkF2Prim
Src2Sink :: src2Label Label2PrimFld[*] _SinkF2Obj

###################
# RULES: LABEL FLOW
###################

# Label-obj flow

Label2Obj :: Label2ObjT
Label2Obj :: Label2ObjX

Label2ObjX :: Label2Obj Obj2RefT Pt
Label2ObjX :: Label2Prim Prim2RefT Pt
Label2ObjX :: Label2PrimFldArr Obj2RefT Pt
Label2ObjX :: Label2ObjX FptArr

# Label-prim flow

Label2Prim :: Label2PrimT
Label2Prim :: Label2Prim _assignPrimCtxt
Label2Prim :: Label2Prim _assignPrimCCtxt

Label2Prim :: Label2Obj Obj2PrimT
Label2Prim :: Label2Prim Prim2PrimT

Label2Prim :: Label2ObjT _Pt _loadPrimCtxt[*]
Label2Prim :: Label2ObjX _Pt _loadPrimCtxtArr
Label2Prim :: Label2PrimFldArr Obj2PrimT

# cl Label2PrimFld[f] o _Pt v_c _loadPrimCtxt[f] u_c
Label2Prim :: Label2PrimFld[f] _Pt _loadPrimCtxt[f]
Label2Prim :: Label2PrimFldStat[f] _loadStatPrimCtxt[f]

# Label-prim_fld flow

Label2PrimFld[f] :: Label2Prim _storePrimCtxt[f] Pt
Label2PrimFldArr :: Label2Prim _storePrimCtxtArr Pt
Label2PrimFldStat[f] :: Label2Prim _storeStatPrimCtxt[f]
*/

/* Normalized Grammar:
Label2Obj:
	Label2Obj :: Label2ObjT
	Label2Obj :: Label2ObjX
Pt:
	Pt :: _newCtxt
	Pt :: _Assign Pt
	Pt :: %1[i] _Fpt[i]
	Pt :: %3[i] _FptStat[i]
%14:
	%14 :: src2Label Label2Prim
Fpt:
	Fpt[i] :: %0[i] Pt
%9:
	%9 :: %8 _Ref2PrimF
%8:
	%8 :: sink2Label Label2Prim
%5:
	%5 :: sink2Label Label2Obj
%4:
	%4 :: _Pt storeArr
%7:
	%7 :: %6 _Ref2RefF
%6:
	%6 :: %5 _Pt
%1:
	%1[i] :: _loadCtxt[i] Pt
%0:
	%0[i] :: _Pt storeCtxt[i]
%3:
	%3[i] :: _loadStatCtxt[i] Pt
%2:
	%2[i] :: _Pt storeStatCtxt[i]
FptStat:
	FptStat[i] :: %2[i] Pt
%20:
	%20 :: Label2ObjX _Pt
%21:
	%21[i] :: Label2PrimFld[i] _Pt
%22:
	%22[i] :: Label2Prim _storePrimCtxt[i]
Label2RefT:
	Label2RefT :: label2RefT
Label2Prim:
	Label2Prim :: Label2PrimT
	Label2Prim :: Label2Prim _assignPrimCtxt
	Label2Prim :: Label2Prim _assignPrimCCtxt
	Label2Prim :: Label2Obj Obj2PrimT
	Label2Prim :: Label2Prim Prim2PrimT
	Label2Prim :: %19 _loadPrimCtxt
	Label2Prim :: %20 _loadPrimCtxtArr
	Label2Prim :: Label2PrimFldArr Obj2PrimT
	Label2Prim :: %21[i] _loadPrimCtxt[i]
	Label2Prim :: Label2PrimFldStat[i] _loadStatPrimCtxt[i]
FptArr:
	FptArr :: %4 Pt
Label2PrimFldStat:
	Label2PrimFldStat[i] :: Label2Prim _storeStatPrimCtxt[i]
SinkF2PrimF:
	SinkF2PrimF :: sinkF2PrimF
Label2ObjT:
	Label2ObjT :: Label2RefT Pt
	Label2ObjT :: Label2ObjT Fpt
Label2ObjX:
	Label2ObjX :: %16 Pt
	Label2ObjX :: %17 Pt
	Label2ObjX :: %18 Pt
	Label2ObjX :: Label2ObjX FptArr
%19:
	%19 :: Label2ObjT _Pt
Label2PrimFld:
	Label2PrimFld[i] :: %22[i] Pt
Src2Sink:
	Src2Sink :: %13 _SinkF2Obj
	Src2Sink :: %14 _SinkF2Prim
	Src2Sink :: %15 _SinkF2Obj
Label2PrimFldArr:
	Label2PrimFldArr :: %23 Pt
Prim2PrimF:
	Prim2PrimF :: prim2PrimF
SinkF2RefF:
	SinkF2RefF :: sinkF2RefF
Obj2PrimT:
	Obj2PrimT :: _Pt Ref2PrimT
	Obj2PrimT :: _FptArr Obj2PrimT
Prim2PrimT:
	Prim2PrimT :: prim2PrimT
Assign:
	Assign :: assignCtxt
	Assign :: assignArgCtxt
	Assign :: assignRetCtxt
Obj2RefT:
	Obj2RefT :: _Pt Ref2RefT
	Obj2RefT :: _FptArr Obj2RefT
Ref2PrimT:
	Ref2PrimT :: ref2PrimT
%18:
	%18 :: Label2PrimFldArr Obj2RefT
SinkF2Prim:
	SinkF2Prim :: SinkF2PrimF
	SinkF2Prim :: %11 _Prim2RefF
	SinkF2Prim :: %12 _Prim2PrimF
Ref2RefF:
	Ref2RefF :: ref2RefF
%23:
	%23 :: Label2Prim _storePrimCtxtArr
%11:
	%11 :: %10 _Pt
%10:
	%10 :: sink2Label Label2Obj
Prim2RefT:
	Prim2RefT :: prim2RefT
%12:
	%12 :: sink2Label Label2Prim
%15:
	%15 :: src2Label Label2PrimFld
SinkF2Obj:
	SinkF2Obj :: SinkF2RefF Pt
	SinkF2Obj :: %7 Pt
	SinkF2Obj :: %9 Pt
	SinkF2Obj :: SinkF2Obj Fpt
%17:
	%17 :: Label2Prim Prim2RefT
%16:
	%16 :: Label2Obj Obj2RefT
Ref2PrimF:
	Ref2PrimF :: ref2PrimF
Label2PrimT:
	Label2PrimT :: label2PrimT
Ref2RefT:
	Ref2RefT :: ref2RefT
Prim2RefF:
	Prim2RefF :: prim2RefF
%13:
	%13 :: src2Label Label2Obj
*/

/* Reverse Productions:
ref2PrimT:
	ref2PrimT => Ref2PrimT
%13:
	%13 + (* _SinkF2Obj) => Src2Sink
sink2Label:
	sink2Label + (* Label2Obj) => %5
	sink2Label + (* Label2Prim) => %8
	sink2Label + (* Label2Obj) => %10
	sink2Label + (* Label2Prim) => %12
assignRetCtxt:
	assignRetCtxt => Assign
ref2RefT:
	ref2RefT => Ref2RefT
Pt:
	Pt + (_Assign *) => Pt
	_Pt + (* storeCtxt[i]) => %0[i]
	Pt + (%0[i] *) => Fpt[i]
	Pt + (_loadCtxt[i] *) => %1[i]
	_Pt + (* storeStatCtxt[i]) => %2[i]
	Pt + (%2[i] *) => FptStat[i]
	Pt + (_loadStatCtxt[i] *) => %3[i]
	_Pt + (* storeArr) => %4
	Pt + (%4 *) => FptArr
	_Pt + (* Ref2RefT) => Obj2RefT
	_Pt + (* Ref2PrimT) => Obj2PrimT
	Pt + (Label2RefT *) => Label2ObjT
	Pt + (SinkF2RefF *) => SinkF2Obj
	_Pt + (%5 *) => %6
	Pt + (%7 *) => SinkF2Obj
	Pt + (%9 *) => SinkF2Obj
	_Pt + (%10 *) => %11
	Pt + (%16 *) => Label2ObjX
	Pt + (%17 *) => Label2ObjX
	Pt + (%18 *) => Label2ObjX
	_Pt + (Label2ObjT *) => %19
	_Pt + (Label2ObjX *) => %20
	_Pt + (Label2PrimFld[i] *) => %21[i]
	Pt + (%22[i] *) => Label2PrimFld[i]
	Pt + (%23 *) => Label2PrimFldArr
assignPrimCtxt:
	_assignPrimCtxt + (Label2Prim *) => Label2Prim
prim2RefT:
	prim2RefT => Prim2RefT
%20:
	%20 + (* _loadPrimCtxtArr) => Label2Prim
loadPrimCtxtArr:
	_loadPrimCtxtArr + (%20 *) => Label2Prim
%14:
	%14 + (* _SinkF2Prim) => Src2Sink
newCtxt:
	_newCtxt => Pt
Fpt:
	_Fpt[i] + (%1[i] *) => Pt
	Fpt + (Label2ObjT *) => Label2ObjT
	Fpt + (SinkF2Obj *) => SinkF2Obj
ref2PrimF:
	ref2PrimF => Ref2PrimF
label2PrimT:
	label2PrimT => Label2PrimT
%9:
	%9 + (* Pt) => SinkF2Obj
assignArgCtxt:
	assignArgCtxt => Assign
sinkF2PrimF:
	sinkF2PrimF => SinkF2PrimF
prim2RefF:
	prim2RefF => Prim2RefF
%4:
	%4 + (* Pt) => FptArr
%7:
	%7 + (* Pt) => SinkF2Obj
%6:
	%6 + (* _Ref2RefF) => %7
%1:
	%1[i] + (* _Fpt[i]) => Pt
%0:
	%0[i] + (* Pt) => Fpt[i]
%3:
	%3[i] + (* _FptStat[i]) => Pt
%2:
	%2[i] + (* Pt) => FptStat[i]
FptStat:
	_FptStat[i] + (%3[i] *) => Pt
assignCtxt:
	assignCtxt => Assign
loadCtxt:
	_loadCtxt[i] + (* Pt) => %1[i]
%5:
	%5 + (* _Pt) => %6
Label2RefT:
	Label2RefT + (* Pt) => Label2ObjT
Prim2PrimT:
	Prim2PrimT + (Label2Prim *) => Label2Prim
src2Label:
	src2Label + (* Label2Obj) => %13
	src2Label + (* Label2Prim) => %14
	src2Label + (* Label2PrimFld) => %15
Label2Prim:
	Label2Prim + (sink2Label *) => %8
	Label2Prim + (sink2Label *) => %12
	Label2Prim + (src2Label *) => %14
	Label2Prim + (* Prim2RefT) => %17
	Label2Prim + (* _assignPrimCtxt) => Label2Prim
	Label2Prim + (* _assignPrimCCtxt) => Label2Prim
	Label2Prim + (* Prim2PrimT) => Label2Prim
	Label2Prim + (* _storePrimCtxt[i]) => %22[i]
	Label2Prim + (* _storePrimCtxtArr) => %23
	Label2Prim + (* _storeStatPrimCtxt[i]) => Label2PrimFldStat[i]
storeStatPrimCtxt:
	_storeStatPrimCtxt[i] + (Label2Prim *) => Label2PrimFldStat[i]
FptArr:
	_FptArr + (* Obj2RefT) => Obj2RefT
	_FptArr + (* Obj2PrimT) => Obj2PrimT
	FptArr + (Label2ObjX *) => Label2ObjX
sinkF2RefF:
	sinkF2RefF => SinkF2RefF
Label2PrimFldStat:
	Label2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Label2Prim
SinkF2PrimF:
	SinkF2PrimF => SinkF2Prim
Label2ObjT:
	Label2ObjT + (* Fpt) => Label2ObjT
	Label2ObjT => Label2Obj
	Label2ObjT + (* _Pt) => %19
%21:
	%21[i] + (* _loadPrimCtxt[i]) => Label2Prim
Label2PrimFld:
	Label2PrimFld + (src2Label *) => %15
	Label2PrimFld[i] + (* _Pt) => %21[i]
Label2ObjX:
	Label2ObjX => Label2Obj
	Label2ObjX + (* FptArr) => Label2ObjX
	Label2ObjX + (* _Pt) => %20
storePrimCtxt:
	_storePrimCtxt[i] + (Label2Prim *) => %22[i]
label2RefT:
	label2RefT => Label2RefT
loadStatPrimCtxt:
	_loadStatPrimCtxt[i] + (Label2PrimFldStat[i] *) => Label2Prim
Label2PrimFldArr:
	Label2PrimFldArr + (* Obj2RefT) => %18
	Label2PrimFldArr + (* Obj2PrimT) => Label2Prim
Prim2PrimF:
	_Prim2PrimF + (%12 *) => SinkF2Prim
storePrimCtxtArr:
	_storePrimCtxtArr + (Label2Prim *) => %23
SinkF2RefF:
	SinkF2RefF + (* Pt) => SinkF2Obj
%19:
	%19 + (* _loadPrimCtxt) => Label2Prim
Obj2PrimT:
	Obj2PrimT + (_FptArr *) => Obj2PrimT
	Obj2PrimT + (Label2Obj *) => Label2Prim
	Obj2PrimT + (Label2PrimFldArr *) => Label2Prim
loadPrimCtxt:
	_loadPrimCtxt + (%19 *) => Label2Prim
	_loadPrimCtxt[i] + (%21[i] *) => Label2Prim
loadStatCtxt:
	_loadStatCtxt[i] + (* Pt) => %3[i]
storeArr:
	storeArr + (_Pt *) => %4
Assign:
	_Assign + (* Pt) => Pt
Obj2RefT:
	Obj2RefT + (_FptArr *) => Obj2RefT
	Obj2RefT + (Label2Obj *) => %16
	Obj2RefT + (Label2PrimFldArr *) => %18
Prim2RefT:
	Prim2RefT + (Label2Prim *) => %17
Ref2PrimT:
	Ref2PrimT + (_Pt *) => Obj2PrimT
%18:
	%18 + (* Pt) => Label2ObjX
SinkF2Prim:
	_SinkF2Prim + (%14 *) => Src2Sink
Ref2RefF:
	_Ref2RefF + (%6 *) => %7
storeStatCtxt:
	storeStatCtxt[i] + (_Pt *) => %2[i]
storeCtxt:
	storeCtxt[i] + (_Pt *) => %0[i]
%11:
	%11 + (* _Prim2RefF) => SinkF2Prim
%10:
	%10 + (* _Pt) => %11
ref2RefF:
	ref2RefF => Ref2RefF
%12:
	%12 + (* _Prim2PrimF) => SinkF2Prim
%15:
	%15 + (* _SinkF2Obj) => Src2Sink
SinkF2Obj:
	SinkF2Obj + (* Fpt) => SinkF2Obj
	_SinkF2Obj + (%13 *) => Src2Sink
	_SinkF2Obj + (%15 *) => Src2Sink
prim2PrimF:
	prim2PrimF => Prim2PrimF
%16:
	%16 + (* Pt) => Label2ObjX
assignPrimCCtxt:
	_assignPrimCCtxt + (Label2Prim *) => Label2Prim
Ref2PrimF:
	_Ref2PrimF + (%8 *) => %9
Label2PrimT:
	Label2PrimT => Label2Prim
Ref2RefT:
	Ref2RefT + (_Pt *) => Obj2RefT
%22:
	%22[i] + (* Pt) => Label2PrimFld[i]
Prim2RefF:
	_Prim2RefF + (%11 *) => SinkF2Prim
%17:
	%17 + (* Pt) => Label2ObjX
%23:
	%23 + (* Pt) => Label2PrimFldArr
prim2PrimT:
	prim2PrimT => Prim2PrimT
Label2Obj:
	Label2Obj + (sink2Label *) => %5
	Label2Obj + (sink2Label *) => %10
	Label2Obj + (src2Label *) => %13
	Label2Obj + (* Obj2RefT) => %16
	Label2Obj + (* Obj2PrimT) => Label2Prim
%8:
	%8 + (* _Ref2PrimF) => %9
*/

public class CG extends Graph {

public boolean isTerminal(int kind) {
  switch (kind) {
  case 1:
  case 2:
  case 3:
  case 5:
  case 7:
  case 9:
  case 12:
  case 14:
  case 17:
  case 20:
  case 22:
  case 24:
  case 26:
  case 28:
  case 30:
  case 32:
  case 34:
  case 36:
  case 38:
  case 40:
  case 42:
  case 47:
  case 60:
  case 70:
  case 71:
  case 72:
  case 74:
  case 78:
  case 79:
  case 81:
  case 83:
    return true;
  default:
    return false;
  }
}

public int numKinds() {
  return 84;
}

public int symbolToKind(String symbol) {
  if (symbol.equals("Assign")) return 0;
  if (symbol.equals("assignCtxt")) return 1;
  if (symbol.equals("assignArgCtxt")) return 2;
  if (symbol.equals("assignRetCtxt")) return 3;
  if (symbol.equals("Pt")) return 4;
  if (symbol.equals("newCtxt")) return 5;
  if (symbol.equals("Fpt")) return 6;
  if (symbol.equals("storeCtxt")) return 7;
  if (symbol.equals("%0")) return 8;
  if (symbol.equals("loadCtxt")) return 9;
  if (symbol.equals("%1")) return 10;
  if (symbol.equals("FptStat")) return 11;
  if (symbol.equals("storeStatCtxt")) return 12;
  if (symbol.equals("%2")) return 13;
  if (symbol.equals("loadStatCtxt")) return 14;
  if (symbol.equals("%3")) return 15;
  if (symbol.equals("FptArr")) return 16;
  if (symbol.equals("storeArr")) return 17;
  if (symbol.equals("%4")) return 18;
  if (symbol.equals("Label2RefT")) return 19;
  if (symbol.equals("label2RefT")) return 20;
  if (symbol.equals("Label2PrimT")) return 21;
  if (symbol.equals("label2PrimT")) return 22;
  if (symbol.equals("SinkF2RefF")) return 23;
  if (symbol.equals("sinkF2RefF")) return 24;
  if (symbol.equals("SinkF2PrimF")) return 25;
  if (symbol.equals("sinkF2PrimF")) return 26;
  if (symbol.equals("Ref2RefF")) return 27;
  if (symbol.equals("ref2RefF")) return 28;
  if (symbol.equals("Ref2PrimF")) return 29;
  if (symbol.equals("ref2PrimF")) return 30;
  if (symbol.equals("Prim2RefF")) return 31;
  if (symbol.equals("prim2RefF")) return 32;
  if (symbol.equals("Prim2PrimF")) return 33;
  if (symbol.equals("prim2PrimF")) return 34;
  if (symbol.equals("Ref2RefT")) return 35;
  if (symbol.equals("ref2RefT")) return 36;
  if (symbol.equals("Ref2PrimT")) return 37;
  if (symbol.equals("ref2PrimT")) return 38;
  if (symbol.equals("Prim2RefT")) return 39;
  if (symbol.equals("prim2RefT")) return 40;
  if (symbol.equals("Prim2PrimT")) return 41;
  if (symbol.equals("prim2PrimT")) return 42;
  if (symbol.equals("Obj2RefT")) return 43;
  if (symbol.equals("Obj2PrimT")) return 44;
  if (symbol.equals("Label2ObjT")) return 45;
  if (symbol.equals("SinkF2Obj")) return 46;
  if (symbol.equals("sink2Label")) return 47;
  if (symbol.equals("Label2Obj")) return 48;
  if (symbol.equals("%5")) return 49;
  if (symbol.equals("%6")) return 50;
  if (symbol.equals("%7")) return 51;
  if (symbol.equals("Label2Prim")) return 52;
  if (symbol.equals("%8")) return 53;
  if (symbol.equals("%9")) return 54;
  if (symbol.equals("SinkF2Prim")) return 55;
  if (symbol.equals("%10")) return 56;
  if (symbol.equals("%11")) return 57;
  if (symbol.equals("%12")) return 58;
  if (symbol.equals("Src2Sink")) return 59;
  if (symbol.equals("src2Label")) return 60;
  if (symbol.equals("%13")) return 61;
  if (symbol.equals("%14")) return 62;
  if (symbol.equals("Label2PrimFld")) return 63;
  if (symbol.equals("%15")) return 64;
  if (symbol.equals("Label2ObjX")) return 65;
  if (symbol.equals("%16")) return 66;
  if (symbol.equals("%17")) return 67;
  if (symbol.equals("Label2PrimFldArr")) return 68;
  if (symbol.equals("%18")) return 69;
  if (symbol.equals("assignPrimCtxt")) return 70;
  if (symbol.equals("assignPrimCCtxt")) return 71;
  if (symbol.equals("loadPrimCtxt")) return 72;
  if (symbol.equals("%19")) return 73;
  if (symbol.equals("loadPrimCtxtArr")) return 74;
  if (symbol.equals("%20")) return 75;
  if (symbol.equals("%21")) return 76;
  if (symbol.equals("Label2PrimFldStat")) return 77;
  if (symbol.equals("loadStatPrimCtxt")) return 78;
  if (symbol.equals("storePrimCtxt")) return 79;
  if (symbol.equals("%22")) return 80;
  if (symbol.equals("storePrimCtxtArr")) return 81;
  if (symbol.equals("%23")) return 82;
  if (symbol.equals("storeStatPrimCtxt")) return 83;
  throw new RuntimeException("Unknown symbol "+symbol);
}

public String kindToSymbol(int kind) {
  switch (kind) {
  case 0: return "Assign";
  case 1: return "assignCtxt";
  case 2: return "assignArgCtxt";
  case 3: return "assignRetCtxt";
  case 4: return "Pt";
  case 5: return "newCtxt";
  case 6: return "Fpt";
  case 7: return "storeCtxt";
  case 8: return "%0";
  case 9: return "loadCtxt";
  case 10: return "%1";
  case 11: return "FptStat";
  case 12: return "storeStatCtxt";
  case 13: return "%2";
  case 14: return "loadStatCtxt";
  case 15: return "%3";
  case 16: return "FptArr";
  case 17: return "storeArr";
  case 18: return "%4";
  case 19: return "Label2RefT";
  case 20: return "label2RefT";
  case 21: return "Label2PrimT";
  case 22: return "label2PrimT";
  case 23: return "SinkF2RefF";
  case 24: return "sinkF2RefF";
  case 25: return "SinkF2PrimF";
  case 26: return "sinkF2PrimF";
  case 27: return "Ref2RefF";
  case 28: return "ref2RefF";
  case 29: return "Ref2PrimF";
  case 30: return "ref2PrimF";
  case 31: return "Prim2RefF";
  case 32: return "prim2RefF";
  case 33: return "Prim2PrimF";
  case 34: return "prim2PrimF";
  case 35: return "Ref2RefT";
  case 36: return "ref2RefT";
  case 37: return "Ref2PrimT";
  case 38: return "ref2PrimT";
  case 39: return "Prim2RefT";
  case 40: return "prim2RefT";
  case 41: return "Prim2PrimT";
  case 42: return "prim2PrimT";
  case 43: return "Obj2RefT";
  case 44: return "Obj2PrimT";
  case 45: return "Label2ObjT";
  case 46: return "SinkF2Obj";
  case 47: return "sink2Label";
  case 48: return "Label2Obj";
  case 49: return "%5";
  case 50: return "%6";
  case 51: return "%7";
  case 52: return "Label2Prim";
  case 53: return "%8";
  case 54: return "%9";
  case 55: return "SinkF2Prim";
  case 56: return "%10";
  case 57: return "%11";
  case 58: return "%12";
  case 59: return "Src2Sink";
  case 60: return "src2Label";
  case 61: return "%13";
  case 62: return "%14";
  case 63: return "Label2PrimFld";
  case 64: return "%15";
  case 65: return "Label2ObjX";
  case 66: return "%16";
  case 67: return "%17";
  case 68: return "Label2PrimFldArr";
  case 69: return "%18";
  case 70: return "assignPrimCtxt";
  case 71: return "assignPrimCCtxt";
  case 72: return "loadPrimCtxt";
  case 73: return "%19";
  case 74: return "loadPrimCtxtArr";
  case 75: return "%20";
  case 76: return "%21";
  case 77: return "Label2PrimFldStat";
  case 78: return "loadStatPrimCtxt";
  case 79: return "storePrimCtxt";
  case 80: return "%22";
  case 81: return "storePrimCtxtArr";
  case 82: return "%23";
  case 83: return "storeStatPrimCtxt";
  default: throw new RuntimeException("Unknown kind "+kind);
  }
}

public void process(Edge base) {
  switch (base.kind) {
  case 0: /* Assign */
    /* _Assign + (* Pt) => Pt */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(base.to, other.to, 4, base, other, false);
    }
    break;
  case 1: /* assignCtxt */
    /* assignCtxt => Assign */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 2: /* assignArgCtxt */
    /* assignArgCtxt => Assign */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 3: /* assignRetCtxt */
    /* assignRetCtxt => Assign */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 4: /* Pt */
    /* Pt + (_Assign *) => Pt */
    for(Edge other : base.from.getOutEdges(0)){
      addEdge(other.to, base.to, 4, base, other, false);
    }
    /* _Pt + (* storeCtxt[i]) => %0[i] */
    for(Edge other : base.from.getOutEdges(7)){
      addEdge(base.to, other.to, 8, base, other, true);
    }
    /* Pt + (%0[i] *) => Fpt[i] */
    for(Edge other : base.from.getInEdges(8)){
      addEdge(other.from, base.to, 6, base, other, true);
    }
    /* Pt + (_loadCtxt[i] *) => %1[i] */
    for(Edge other : base.from.getOutEdges(9)){
      addEdge(other.to, base.to, 10, base, other, true);
    }
    /* _Pt + (* storeStatCtxt[i]) => %2[i] */
    for(Edge other : base.from.getOutEdges(12)){
      addEdge(base.to, other.to, 13, base, other, true);
    }
    /* Pt + (%2[i] *) => FptStat[i] */
    for(Edge other : base.from.getInEdges(13)){
      addEdge(other.from, base.to, 11, base, other, true);
    }
    /* Pt + (_loadStatCtxt[i] *) => %3[i] */
    for(Edge other : base.from.getOutEdges(14)){
      addEdge(other.to, base.to, 15, base, other, true);
    }
    /* _Pt + (* storeArr) => %4 */
    for(Edge other : base.from.getOutEdges(17)){
      addEdge(base.to, other.to, 18, base, other, false);
    }
    /* Pt + (%4 *) => FptArr */
    for(Edge other : base.from.getInEdges(18)){
      addEdge(other.from, base.to, 16, base, other, false);
    }
    /* _Pt + (* Ref2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(35)){
      addEdge(base.to, other.to, 43, base, other, false);
    }
    /* _Pt + (* Ref2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(37)){
      addEdge(base.to, other.to, 44, base, other, false);
    }
    /* Pt + (Label2RefT *) => Label2ObjT */
    for(Edge other : base.from.getInEdges(19)){
      addEdge(other.from, base.to, 45, base, other, false);
    }
    /* Pt + (SinkF2RefF *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(23)){
      addEdge(other.from, base.to, 46, base, other, false);
    }
    /* _Pt + (%5 *) => %6 */
    for(Edge other : base.to.getInEdges(49)){
      addEdge(other.from, base.from, 50, base, other, false);
    }
    /* Pt + (%7 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(51)){
      addEdge(other.from, base.to, 46, base, other, false);
    }
    /* Pt + (%9 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(54)){
      addEdge(other.from, base.to, 46, base, other, false);
    }
    /* _Pt + (%10 *) => %11 */
    for(Edge other : base.to.getInEdges(56)){
      addEdge(other.from, base.from, 57, base, other, false);
    }
    /* Pt + (%16 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(66)){
      addEdge(other.from, base.to, 65, base, other, false);
    }
    /* Pt + (%17 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(67)){
      addEdge(other.from, base.to, 65, base, other, false);
    }
    /* Pt + (%18 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(69)){
      addEdge(other.from, base.to, 65, base, other, false);
    }
    /* _Pt + (Label2ObjT *) => %19 */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 73, base, other, false);
    }
    /* _Pt + (Label2ObjX *) => %20 */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(other.from, base.from, 75, base, other, false);
    }
    /* _Pt + (Label2PrimFld[i] *) => %21[i] */
    for(Edge other : base.to.getInEdges(63)){
      addEdge(other.from, base.from, 76, base, other, true);
    }
    /* Pt + (%22[i] *) => Label2PrimFld[i] */
    for(Edge other : base.from.getInEdges(80)){
      addEdge(other.from, base.to, 63, base, other, true);
    }
    /* Pt + (%23 *) => Label2PrimFldArr */
    for(Edge other : base.from.getInEdges(82)){
      addEdge(other.from, base.to, 68, base, other, false);
    }
    break;
  case 5: /* newCtxt */
    /* _newCtxt => Pt */
    addEdge(base.to, base.from, 4, base, false);
    break;
  case 6: /* Fpt */
    /* _Fpt[i] + (%1[i] *) => Pt */
    for(Edge other : base.to.getInEdges(10)){
      addEdge(other.from, base.from, 4, base, other, false);
    }
    /* Fpt + (Label2ObjT *) => Label2ObjT */
    for(Edge other : base.from.getInEdges(45)){
      addEdge(other.from, base.to, 45, base, other, false);
    }
    /* Fpt + (SinkF2Obj *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(46)){
      addEdge(other.from, base.to, 46, base, other, false);
    }
    break;
  case 7: /* storeCtxt */
    /* storeCtxt[i] + (_Pt *) => %0[i] */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(other.to, base.to, 8, base, other, true);
    }
    break;
  case 8: /* %0 */
    /* %0[i] + (* Pt) => Fpt[i] */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 6, base, other, true);
    }
    break;
  case 9: /* loadCtxt */
    /* _loadCtxt[i] + (* Pt) => %1[i] */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(base.to, other.to, 10, base, other, true);
    }
    break;
  case 10: /* %1 */
    /* %1[i] + (* _Fpt[i]) => Pt */
    for(Edge other : base.to.getInEdges(6)){
      addEdge(base.from, other.from, 4, base, other, false);
    }
    break;
  case 11: /* FptStat */
    /* _FptStat[i] + (%3[i] *) => Pt */
    for(Edge other : base.to.getInEdges(15)){
      addEdge(other.from, base.from, 4, base, other, false);
    }
    break;
  case 12: /* storeStatCtxt */
    /* storeStatCtxt[i] + (_Pt *) => %2[i] */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(other.to, base.to, 13, base, other, true);
    }
    break;
  case 13: /* %2 */
    /* %2[i] + (* Pt) => FptStat[i] */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 11, base, other, true);
    }
    break;
  case 14: /* loadStatCtxt */
    /* _loadStatCtxt[i] + (* Pt) => %3[i] */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(base.to, other.to, 15, base, other, true);
    }
    break;
  case 15: /* %3 */
    /* %3[i] + (* _FptStat[i]) => Pt */
    for(Edge other : base.to.getInEdges(11)){
      addEdge(base.from, other.from, 4, base, other, false);
    }
    break;
  case 16: /* FptArr */
    /* _FptArr + (* Obj2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(43)){
      addEdge(base.to, other.to, 43, base, other, false);
    }
    /* _FptArr + (* Obj2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(44)){
      addEdge(base.to, other.to, 44, base, other, false);
    }
    /* FptArr + (Label2ObjX *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(65)){
      addEdge(other.from, base.to, 65, base, other, false);
    }
    break;
  case 17: /* storeArr */
    /* storeArr + (_Pt *) => %4 */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(other.to, base.to, 18, base, other, false);
    }
    break;
  case 18: /* %4 */
    /* %4 + (* Pt) => FptArr */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 16, base, other, false);
    }
    break;
  case 19: /* Label2RefT */
    /* Label2RefT + (* Pt) => Label2ObjT */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 45, base, other, false);
    }
    break;
  case 20: /* label2RefT */
    /* label2RefT => Label2RefT */
    addEdge(base.from, base.to, 19, base, false);
    break;
  case 21: /* Label2PrimT */
    /* Label2PrimT => Label2Prim */
    addEdge(base.from, base.to, 52, base, false);
    break;
  case 22: /* label2PrimT */
    /* label2PrimT => Label2PrimT */
    addEdge(base.from, base.to, 21, base, false);
    break;
  case 23: /* SinkF2RefF */
    /* SinkF2RefF + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 46, base, other, false);
    }
    break;
  case 24: /* sinkF2RefF */
    /* sinkF2RefF => SinkF2RefF */
    addEdge(base.from, base.to, 23, base, false);
    break;
  case 25: /* SinkF2PrimF */
    /* SinkF2PrimF => SinkF2Prim */
    addEdge(base.from, base.to, 55, base, false);
    break;
  case 26: /* sinkF2PrimF */
    /* sinkF2PrimF => SinkF2PrimF */
    addEdge(base.from, base.to, 25, base, false);
    break;
  case 27: /* Ref2RefF */
    /* _Ref2RefF + (%6 *) => %7 */
    for(Edge other : base.to.getInEdges(50)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    break;
  case 28: /* ref2RefF */
    /* ref2RefF => Ref2RefF */
    addEdge(base.from, base.to, 27, base, false);
    break;
  case 29: /* Ref2PrimF */
    /* _Ref2PrimF + (%8 *) => %9 */
    for(Edge other : base.to.getInEdges(53)){
      addEdge(other.from, base.from, 54, base, other, false);
    }
    break;
  case 30: /* ref2PrimF */
    /* ref2PrimF => Ref2PrimF */
    addEdge(base.from, base.to, 29, base, false);
    break;
  case 31: /* Prim2RefF */
    /* _Prim2RefF + (%11 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(57)){
      addEdge(other.from, base.from, 55, base, other, false);
    }
    break;
  case 32: /* prim2RefF */
    /* prim2RefF => Prim2RefF */
    addEdge(base.from, base.to, 31, base, false);
    break;
  case 33: /* Prim2PrimF */
    /* _Prim2PrimF + (%12 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 55, base, other, false);
    }
    break;
  case 34: /* prim2PrimF */
    /* prim2PrimF => Prim2PrimF */
    addEdge(base.from, base.to, 33, base, false);
    break;
  case 35: /* Ref2RefT */
    /* Ref2RefT + (_Pt *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(other.to, base.to, 43, base, other, false);
    }
    break;
  case 36: /* ref2RefT */
    /* ref2RefT => Ref2RefT */
    addEdge(base.from, base.to, 35, base, false);
    break;
  case 37: /* Ref2PrimT */
    /* Ref2PrimT + (_Pt *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(other.to, base.to, 44, base, other, false);
    }
    break;
  case 38: /* ref2PrimT */
    /* ref2PrimT => Ref2PrimT */
    addEdge(base.from, base.to, 37, base, false);
    break;
  case 39: /* Prim2RefT */
    /* Prim2RefT + (Label2Prim *) => %17 */
    for(Edge other : base.from.getInEdges(52)){
      addEdge(other.from, base.to, 67, base, other, false);
    }
    break;
  case 40: /* prim2RefT */
    /* prim2RefT => Prim2RefT */
    addEdge(base.from, base.to, 39, base, false);
    break;
  case 41: /* Prim2PrimT */
    /* Prim2PrimT + (Label2Prim *) => Label2Prim */
    for(Edge other : base.from.getInEdges(52)){
      addEdge(other.from, base.to, 52, base, other, false);
    }
    break;
  case 42: /* prim2PrimT */
    /* prim2PrimT => Prim2PrimT */
    addEdge(base.from, base.to, 41, base, false);
    break;
  case 43: /* Obj2RefT */
    /* Obj2RefT + (_FptArr *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(16)){
      addEdge(other.to, base.to, 43, base, other, false);
    }
    /* Obj2RefT + (Label2Obj *) => %16 */
    for(Edge other : base.from.getInEdges(48)){
      addEdge(other.from, base.to, 66, base, other, false);
    }
    /* Obj2RefT + (Label2PrimFldArr *) => %18 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    break;
  case 44: /* Obj2PrimT */
    /* Obj2PrimT + (_FptArr *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(16)){
      addEdge(other.to, base.to, 44, base, other, false);
    }
    /* Obj2PrimT + (Label2Obj *) => Label2Prim */
    for(Edge other : base.from.getInEdges(48)){
      addEdge(other.from, base.to, 52, base, other, false);
    }
    /* Obj2PrimT + (Label2PrimFldArr *) => Label2Prim */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 52, base, other, false);
    }
    break;
  case 45: /* Label2ObjT */
    /* Label2ObjT + (* Fpt) => Label2ObjT */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 45, base, other, false);
    }
    /* Label2ObjT => Label2Obj */
    addEdge(base.from, base.to, 48, base, false);
    /* Label2ObjT + (* _Pt) => %19 */
    for(Edge other : base.to.getInEdges(4)){
      addEdge(base.from, other.from, 73, base, other, false);
    }
    break;
  case 46: /* SinkF2Obj */
    /* SinkF2Obj + (* Fpt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 46, base, other, false);
    }
    /* _SinkF2Obj + (%13 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 59, base, other, false);
    }
    /* _SinkF2Obj + (%15 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(64)){
      addEdge(other.from, base.from, 59, base, other, false);
    }
    break;
  case 47: /* sink2Label */
    /* sink2Label + (* Label2Obj) => %5 */
    for(Edge other : base.to.getOutEdges(48)){
      addEdge(base.from, other.to, 49, base, other, false);
    }
    /* sink2Label + (* Label2Prim) => %8 */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    /* sink2Label + (* Label2Obj) => %10 */
    for(Edge other : base.to.getOutEdges(48)){
      addEdge(base.from, other.to, 56, base, other, false);
    }
    /* sink2Label + (* Label2Prim) => %12 */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 58, base, other, false);
    }
    break;
  case 48: /* Label2Obj */
    /* Label2Obj + (sink2Label *) => %5 */
    for(Edge other : base.from.getInEdges(47)){
      addEdge(other.from, base.to, 49, base, other, false);
    }
    /* Label2Obj + (sink2Label *) => %10 */
    for(Edge other : base.from.getInEdges(47)){
      addEdge(other.from, base.to, 56, base, other, false);
    }
    /* Label2Obj + (src2Label *) => %13 */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    /* Label2Obj + (* Obj2RefT) => %16 */
    for(Edge other : base.to.getOutEdges(43)){
      addEdge(base.from, other.to, 66, base, other, false);
    }
    /* Label2Obj + (* Obj2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(44)){
      addEdge(base.from, other.to, 52, base, other, false);
    }
    break;
  case 49: /* %5 */
    /* %5 + (* _Pt) => %6 */
    for(Edge other : base.to.getInEdges(4)){
      addEdge(base.from, other.from, 50, base, other, false);
    }
    break;
  case 50: /* %6 */
    /* %6 + (* _Ref2RefF) => %7 */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    break;
  case 51: /* %7 */
    /* %7 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 46, base, other, false);
    }
    break;
  case 52: /* Label2Prim */
    /* Label2Prim + (sink2Label *) => %8 */
    for(Edge other : base.from.getInEdges(47)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* Label2Prim + (sink2Label *) => %12 */
    for(Edge other : base.from.getInEdges(47)){
      addEdge(other.from, base.to, 58, base, other, false);
    }
    /* Label2Prim + (src2Label *) => %14 */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 62, base, other, false);
    }
    /* Label2Prim + (* Prim2RefT) => %17 */
    for(Edge other : base.to.getOutEdges(39)){
      addEdge(base.from, other.to, 67, base, other, false);
    }
    /* Label2Prim + (* _assignPrimCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(70)){
      addEdge(base.from, other.from, 52, base, other, false);
    }
    /* Label2Prim + (* _assignPrimCCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(71)){
      addEdge(base.from, other.from, 52, base, other, false);
    }
    /* Label2Prim + (* Prim2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(41)){
      addEdge(base.from, other.to, 52, base, other, false);
    }
    /* Label2Prim + (* _storePrimCtxt[i]) => %22[i] */
    for(Edge other : base.to.getInEdges(79)){
      addEdge(base.from, other.from, 80, base, other, true);
    }
    /* Label2Prim + (* _storePrimCtxtArr) => %23 */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(base.from, other.from, 82, base, other, false);
    }
    /* Label2Prim + (* _storeStatPrimCtxt[i]) => Label2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(83)){
      addEdge(base.from, other.from, 77, base, other, true);
    }
    break;
  case 53: /* %8 */
    /* %8 + (* _Ref2PrimF) => %9 */
    for(Edge other : base.to.getInEdges(29)){
      addEdge(base.from, other.from, 54, base, other, false);
    }
    break;
  case 54: /* %9 */
    /* %9 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 46, base, other, false);
    }
    break;
  case 55: /* SinkF2Prim */
    /* _SinkF2Prim + (%14 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(62)){
      addEdge(other.from, base.from, 59, base, other, false);
    }
    break;
  case 56: /* %10 */
    /* %10 + (* _Pt) => %11 */
    for(Edge other : base.to.getInEdges(4)){
      addEdge(base.from, other.from, 57, base, other, false);
    }
    break;
  case 57: /* %11 */
    /* %11 + (* _Prim2RefF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(31)){
      addEdge(base.from, other.from, 55, base, other, false);
    }
    break;
  case 58: /* %12 */
    /* %12 + (* _Prim2PrimF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(33)){
      addEdge(base.from, other.from, 55, base, other, false);
    }
    break;
  case 60: /* src2Label */
    /* src2Label + (* Label2Obj) => %13 */
    for(Edge other : base.to.getOutEdges(48)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    /* src2Label + (* Label2Prim) => %14 */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 62, base, other, false);
    }
    /* src2Label + (* Label2PrimFld) => %15 */
    for(Edge other : base.to.getOutEdges(63)){
      addEdge(base.from, other.to, 64, base, other, false);
    }
    break;
  case 61: /* %13 */
    /* %13 + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(46)){
      addEdge(base.from, other.from, 59, base, other, false);
    }
    break;
  case 62: /* %14 */
    /* %14 + (* _SinkF2Prim) => Src2Sink */
    for(Edge other : base.to.getInEdges(55)){
      addEdge(base.from, other.from, 59, base, other, false);
    }
    break;
  case 63: /* Label2PrimFld */
    /* Label2PrimFld + (src2Label *) => %15 */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 64, base, other, false);
    }
    /* Label2PrimFld[i] + (* _Pt) => %21[i] */
    for(Edge other : base.to.getInEdges(4)){
      addEdge(base.from, other.from, 76, base, other, true);
    }
    break;
  case 64: /* %15 */
    /* %15 + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(46)){
      addEdge(base.from, other.from, 59, base, other, false);
    }
    break;
  case 65: /* Label2ObjX */
    /* Label2ObjX => Label2Obj */
    addEdge(base.from, base.to, 48, base, false);
    /* Label2ObjX + (* FptArr) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(16)){
      addEdge(base.from, other.to, 65, base, other, false);
    }
    /* Label2ObjX + (* _Pt) => %20 */
    for(Edge other : base.to.getInEdges(4)){
      addEdge(base.from, other.from, 75, base, other, false);
    }
    break;
  case 66: /* %16 */
    /* %16 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 65, base, other, false);
    }
    break;
  case 67: /* %17 */
    /* %17 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 65, base, other, false);
    }
    break;
  case 68: /* Label2PrimFldArr */
    /* Label2PrimFldArr + (* Obj2RefT) => %18 */
    for(Edge other : base.to.getOutEdges(43)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    /* Label2PrimFldArr + (* Obj2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(44)){
      addEdge(base.from, other.to, 52, base, other, false);
    }
    break;
  case 69: /* %18 */
    /* %18 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 65, base, other, false);
    }
    break;
  case 70: /* assignPrimCtxt */
    /* _assignPrimCtxt + (Label2Prim *) => Label2Prim */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 52, base, other, false);
    }
    break;
  case 71: /* assignPrimCCtxt */
    /* _assignPrimCCtxt + (Label2Prim *) => Label2Prim */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 52, base, other, false);
    }
    break;
  case 72: /* loadPrimCtxt */
    /* _loadPrimCtxt + (%19 *) => Label2Prim */
    for(Edge other : base.to.getInEdges(73)){
      addEdge(other.from, base.from, 52, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%21[i] *) => Label2Prim */
    for(Edge other : base.to.getInEdges(76)){
      addEdge(other.from, base.from, 52, base, other, false);
    }
    break;
  case 73: /* %19 */
    /* %19 + (* _loadPrimCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(72)){
      addEdge(base.from, other.from, 52, base, other, false);
    }
    break;
  case 74: /* loadPrimCtxtArr */
    /* _loadPrimCtxtArr + (%20 *) => Label2Prim */
    for(Edge other : base.to.getInEdges(75)){
      addEdge(other.from, base.from, 52, base, other, false);
    }
    break;
  case 75: /* %20 */
    /* %20 + (* _loadPrimCtxtArr) => Label2Prim */
    for(Edge other : base.to.getInEdges(74)){
      addEdge(base.from, other.from, 52, base, other, false);
    }
    break;
  case 76: /* %21 */
    /* %21[i] + (* _loadPrimCtxt[i]) => Label2Prim */
    for(Edge other : base.to.getInEdges(72)){
      addEdge(base.from, other.from, 52, base, other, false);
    }
    break;
  case 77: /* Label2PrimFldStat */
    /* Label2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Label2Prim */
    for(Edge other : base.to.getInEdges(78)){
      addEdge(base.from, other.from, 52, base, other, false);
    }
    break;
  case 78: /* loadStatPrimCtxt */
    /* _loadStatPrimCtxt[i] + (Label2PrimFldStat[i] *) => Label2Prim */
    for(Edge other : base.to.getInEdges(77)){
      addEdge(other.from, base.from, 52, base, other, false);
    }
    break;
  case 79: /* storePrimCtxt */
    /* _storePrimCtxt[i] + (Label2Prim *) => %22[i] */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 80, base, other, true);
    }
    break;
  case 80: /* %22 */
    /* %22[i] + (* Pt) => Label2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 63, base, other, true);
    }
    break;
  case 81: /* storePrimCtxtArr */
    /* _storePrimCtxtArr + (Label2Prim *) => %23 */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 82, base, other, false);
    }
    break;
  case 82: /* %23 */
    /* %23 + (* Pt) => Label2PrimFldArr */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 68, base, other, false);
    }
    break;
  case 83: /* storeStatPrimCtxt */
    /* _storeStatPrimCtxt[i] + (Label2Prim *) => Label2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 77, base, other, true);
    }
    break;
  }
}

public String[] outputRels() {
    String[] rels = {};
    return rels;
}

public short kindToWeight(int kind) {
  switch (kind) {
  default:
    return (short)0;
  }
}

public boolean useReps() { return true; }

}