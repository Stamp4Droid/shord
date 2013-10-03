package stamp.missingmodels.grammars;
import stamp.missingmodels.util.jcflsolver.*;

/* Original Grammar:
###################
# INPUTS
###################

# source annotations: src2RefT, src2PrimT
# sink annotations: sink2RefT, sink2PrimT, sinkF2RefF, sinkF2PrimF
# transfer annotations: ref2RefT, ref2PrimT, prim2RefT, prim2PrimT 
# flow annotations: ref2RefF, ref2PrimF, prim2RefF, prim2PrimF

# pt: pt, fptArr
# field: fpt
# helper: assignPrimCtxt, assignPrimCCtxt, loadPrimCtxtArr, storePrimCtxtArr
# field helper: loadPrimCtxt, loadStatPrimCtxt, storePrimCtxt, storeStatPrimCtxt

###################
# RULES: OBJECT ANNOTATIONS
###################

# TODO: define fptArr, loadPrimCtxtArr
# TODO: define conversion for inputs
# TODO: run and print output

Obj2RefT :: _pt ref2RefT
Obj2PrimT :: _pt ref2PrimT
Obj2RefT :: _fptArr Obj2RefT
Obj2PrimT :: _fptArr Obj2PrimT

Src2ObjT :: src2RefT pt
Src2ObjT :: Src2ObjT fpt[*]

Sink2ObjT :: sink2RefT pt
Sink2ObjT :: Sink2ObjT fpt[*]

###################
# RULES: SINKF
###################

# Sink_full-obj flow

SinkF2Obj :: sinkF2RefF pt
SinkF2Obj :: Sink2Obj _pt _ref2RefF pt
SinkF2Obj :: Sink2Prim _ref2PrimF pt
SinkF2Obj :: SinkF2Obj fpt[*]

# Sink_full-prim flow

SinkF2Prim :: sinkF2PrimF
SinkF2Prim :: Sink2Obj _pt _prim2RefF
SinkF2Prim :: Sink2Prim _prim2PrimF

###################
# RULES: SRC-SINK FLOW
###################

Src2Sink :: Src2Obj _SinkF2Obj
Src2Sink :: Src2Prim _SinkF2Prim
Src2Sink :: Src2PrimFld[*] _SinkF2Obj

###################
# RULES: LABEL FLOW
###################

# Src-obj flow

Src2Obj :: Src2ObjT
Src2Obj :: Src2ObjX

Src2ObjX :: Src2Obj Obj2RefT pt
Src2ObjX :: Src2Prim prim2RefT pt
Src2ObjX :: Src2PrimFldArr Obj2RefT pt
Src2ObjX :: Src2ObjX fptArr

# Sink-obj flow

Sink2Obj :: Sink2ObjT
Sink2Obj :: Sink2ObjX

Sink2ObjX :: Sink2Obj Obj2RefT pt
Sink2ObjX :: Sink2Prim prim2RefT pt
Sink2ObjX :: Sink2PrimFldArr Obj2RefT pt
Sink2ObjX :: Sink2ObjX fptArr

# Src-prim flow

Src2Prim :: src2PrimT
Src2Prim :: Src2Prim _assignPrimCtxt
Src2Prim :: Src2Prim _assignPrimCCtxt

Src2Prim :: Src2Obj Obj2PrimT
Src2Prim :: Src2Prim prim2PrimT

Src2Prim :: Src2ObjT _pt _loadPrimCtxt[*]
Src2Prim :: Src2ObjX _pt _loadPrimCtxtArr
Src2Prim :: Src2PrimFldArr Obj2PrimT

# cl Src2PrimFld[f] o _pt v_c _loadPrimCtxt[f] u_c
Src2Prim :: Src2PrimFld[f] _pt _loadPrimCtxt[f]
Src2Prim :: Src2PrimFldStat[f] _loadStatPrimCtxt[f]

# Src-prim_fld flow

Src2PrimFld[f] :: Src2Prim _storePrimCtxt[f] pt
Src2PrimFldArr :: Src2Prim _storePrimCtxtArr pt
Src2PrimFldStat[f] :: Src2Prim _storeStatPrimCtxt[f]

# Sink-prim flow

Sink2Prim :: sink2PrimT
Sink2Prim :: Sink2Prim _assignPrimCtxt
Sink2Prim :: Sink2Prim _assignPrimCCtxt

Sink2Prim :: Sink2Obj Obj2PrimT
Sink2Prim :: Sink2Prim prim2PrimT

Sink2Prim :: Sink2ObjT _pt _loadPrimCtxt[*]
Sink2Prim :: Sink2ObjX _pt _loadPrimCtxtArr
Sink2Prim :: Sink2PrimFldArr Obj2PrimT

# cl Sink2PrimFld[f] o _pt v_c _loadPrimCtxt[f] u_c
Sink2Prim :: Sink2PrimFld[f] _pt _loadPrimCtxt[f]
Sink2Prim :: Sink2PrimFldStat[f] _loadStatPrimCtxt[f]

# Sink-prim_fld flow

Sink2PrimFld[f] :: Sink2Prim _storePrimCtxt[f] pt
Sink2PrimFldArr :: Sink2Prim _storePrimCtxtArr pt
Sink2PrimFldStat[f] :: Sink2Prim _storeStatPrimCtxt[f]

###################
# RULES: OUTPUT
###################

LabelRef3 :: Src2Obj _pt
LabelRef3 :: Sink2Obj _pt
LabelPrim3 :: Src2Prim
LabelPrim3 :: Sink2Prim
Flow3 :: Src2Sink
*/

/* Normalized Grammar:
Src2ObjT:
	Src2ObjT :: src2RefT pt
	Src2ObjT :: Src2ObjT fpt
Sink2PrimFldStat:
	Sink2PrimFldStat[i] :: Sink2Prim _storeStatPrimCtxt[i]
%9:
	%9 :: Sink2PrimFldArr Obj2RefT
%8:
	%8 :: Sink2Prim prim2RefT
%5:
	%5 :: Src2Prim prim2RefT
%4:
	%4 :: Src2Obj Obj2RefT
%7:
	%7 :: Sink2Obj Obj2RefT
%6:
	%6 :: Src2PrimFldArr Obj2RefT
%1:
	%1 :: %0 _ref2RefF
%0:
	%0 :: Sink2Obj _pt
%3:
	%3 :: Sink2Obj _pt
%2:
	%2 :: Sink2Prim _ref2PrimF
Src2PrimFldArr:
	Src2PrimFldArr :: %14 pt
Src2PrimFldStat:
	Src2PrimFldStat[i] :: Src2Prim _storeStatPrimCtxt[i]
Sink2Prim:
	Sink2Prim :: sink2PrimT
	Sink2Prim :: Sink2Prim _assignPrimCtxt
	Sink2Prim :: Sink2Prim _assignPrimCCtxt
	Sink2Prim :: Sink2Obj Obj2PrimT
	Sink2Prim :: Sink2Prim prim2PrimT
	Sink2Prim :: %15 _loadPrimCtxt
	Sink2Prim :: %16 _loadPrimCtxtArr
	Sink2Prim :: Sink2PrimFldArr Obj2PrimT
	Sink2Prim :: %17[i] _loadPrimCtxt[i]
	Sink2Prim :: Sink2PrimFldStat[i] _loadStatPrimCtxt[i]
Sink2PrimFldArr:
	Sink2PrimFldArr :: %19 pt
Sink2PrimFld:
	Sink2PrimFld[i] :: %18[i] pt
Src2Sink:
	Src2Sink :: Src2Obj _SinkF2Obj
	Src2Sink :: Src2Prim _SinkF2Prim
	Src2Sink :: Src2PrimFld _SinkF2Obj
%18:
	%18[i] :: Sink2Prim _storePrimCtxt[i]
Sink2Obj:
	Sink2Obj :: Sink2ObjT
	Sink2Obj :: Sink2ObjX
Src2PrimFld:
	Src2PrimFld[i] :: %13[i] pt
LabelPrim3:
	LabelPrim3 :: Src2Prim
	LabelPrim3 :: Sink2Prim
Sink2ObjT:
	Sink2ObjT :: sink2RefT pt
	Sink2ObjT :: Sink2ObjT fpt
Obj2PrimT:
	Obj2PrimT :: _pt ref2PrimT
	Obj2PrimT :: _fptArr Obj2PrimT
Sink2ObjX:
	Sink2ObjX :: %7 pt
	Sink2ObjX :: %8 pt
	Sink2ObjX :: %9 pt
	Sink2ObjX :: Sink2ObjX fptArr
%14:
	%14 :: Src2Prim _storePrimCtxtArr
Obj2RefT:
	Obj2RefT :: _pt ref2RefT
	Obj2RefT :: _fptArr Obj2RefT
%19:
	%19 :: Sink2Prim _storePrimCtxtArr
Src2ObjX:
	Src2ObjX :: %4 pt
	Src2ObjX :: %5 pt
	Src2ObjX :: %6 pt
	Src2ObjX :: Src2ObjX fptArr
SinkF2Prim:
	SinkF2Prim :: sinkF2PrimF
	SinkF2Prim :: %3 _prim2RefF
	SinkF2Prim :: Sink2Prim _prim2PrimF
Src2Prim:
	Src2Prim :: src2PrimT
	Src2Prim :: Src2Prim _assignPrimCtxt
	Src2Prim :: Src2Prim _assignPrimCCtxt
	Src2Prim :: Src2Obj Obj2PrimT
	Src2Prim :: Src2Prim prim2PrimT
	Src2Prim :: %10 _loadPrimCtxt
	Src2Prim :: %11 _loadPrimCtxtArr
	Src2Prim :: Src2PrimFldArr Obj2PrimT
	Src2Prim :: %12[i] _loadPrimCtxt[i]
	Src2Prim :: Src2PrimFldStat[i] _loadStatPrimCtxt[i]
%11:
	%11 :: Src2ObjX _pt
%10:
	%10 :: Src2ObjT _pt
%13:
	%13[i] :: Src2Prim _storePrimCtxt[i]
%12:
	%12[i] :: Src2PrimFld[i] _pt
%15:
	%15 :: Sink2ObjT _pt
SinkF2Obj:
	SinkF2Obj :: sinkF2RefF pt
	SinkF2Obj :: %1 pt
	SinkF2Obj :: %2 pt
	SinkF2Obj :: SinkF2Obj fpt
%17:
	%17[i] :: Sink2PrimFld[i] _pt
%16:
	%16 :: Sink2ObjX _pt
Flow3:
	Flow3 :: Src2Sink
Src2Obj:
	Src2Obj :: Src2ObjT
	Src2Obj :: Src2ObjX
LabelRef3:
	LabelRef3 :: Src2Obj _pt
	LabelRef3 :: Sink2Obj _pt
*/

/* Reverse Productions:
ref2PrimT:
	ref2PrimT + (_pt *) => Obj2PrimT
storeStatPrimCtxt:
	_storeStatPrimCtxt[i] + (Src2Prim *) => Src2PrimFldStat[i]
	_storeStatPrimCtxt[i] + (Sink2Prim *) => Sink2PrimFldStat[i]
storePrimCtxtArr:
	_storePrimCtxtArr + (Src2Prim *) => %14
	_storePrimCtxtArr + (Sink2Prim *) => %19
ref2RefF:
	_ref2RefF + (%0 *) => %1
ref2RefT:
	ref2RefT + (_pt *) => Obj2RefT
assignPrimCtxt:
	_assignPrimCtxt + (Src2Prim *) => Src2Prim
	_assignPrimCtxt + (Sink2Prim *) => Sink2Prim
prim2RefT:
	prim2RefT + (Src2Prim *) => %5
	prim2RefT + (Sink2Prim *) => %8
Src2ObjT:
	Src2ObjT + (* fpt) => Src2ObjT
	Src2ObjT => Src2Obj
	Src2ObjT + (* _pt) => %10
fptArr:
	_fptArr + (* Obj2RefT) => Obj2RefT
	_fptArr + (* Obj2PrimT) => Obj2PrimT
	fptArr + (Src2ObjX *) => Src2ObjX
	fptArr + (Sink2ObjX *) => Sink2ObjX
ref2PrimF:
	_ref2PrimF + (Sink2Prim *) => %2
Sink2PrimFldStat:
	Sink2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Sink2Prim
%9:
	%9 + (* pt) => Sink2ObjX
%8:
	%8 + (* pt) => Sink2ObjX
sinkF2PrimF:
	sinkF2PrimF => SinkF2Prim
loadPrimCtxt:
	_loadPrimCtxt + (%10 *) => Src2Prim
	_loadPrimCtxt[i] + (%12[i] *) => Src2Prim
	_loadPrimCtxt + (%15 *) => Sink2Prim
	_loadPrimCtxt[i] + (%17[i] *) => Sink2Prim
prim2RefF:
	_prim2RefF + (%3 *) => SinkF2Prim
%4:
	%4 + (* pt) => Src2ObjX
%7:
	%7 + (* pt) => Sink2ObjX
%6:
	%6 + (* pt) => Src2ObjX
Sink2Prim:
	Sink2Prim + (* _ref2PrimF) => %2
	Sink2Prim + (* _prim2PrimF) => SinkF2Prim
	Sink2Prim + (* prim2RefT) => %8
	Sink2Prim + (* _assignPrimCtxt) => Sink2Prim
	Sink2Prim + (* _assignPrimCCtxt) => Sink2Prim
	Sink2Prim + (* prim2PrimT) => Sink2Prim
	Sink2Prim + (* _storePrimCtxt[i]) => %18[i]
	Sink2Prim + (* _storePrimCtxtArr) => %19
	Sink2Prim + (* _storeStatPrimCtxt[i]) => Sink2PrimFldStat[i]
	Sink2Prim => LabelPrim3
%0:
	%0 + (* _ref2RefF) => %1
%3:
	%3 + (* _prim2RefF) => SinkF2Prim
%2:
	%2 + (* pt) => SinkF2Obj
Src2PrimFldArr:
	Src2PrimFldArr + (* Obj2RefT) => %6
	Src2PrimFldArr + (* Obj2PrimT) => Src2Prim
Src2PrimFldStat:
	Src2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Src2Prim
pt:
	_pt + (* ref2RefT) => Obj2RefT
	_pt + (* ref2PrimT) => Obj2PrimT
	pt + (src2RefT *) => Src2ObjT
	pt + (sink2RefT *) => Sink2ObjT
	pt + (sinkF2RefF *) => SinkF2Obj
	_pt + (Sink2Obj *) => %0
	pt + (%1 *) => SinkF2Obj
	pt + (%2 *) => SinkF2Obj
	_pt + (Sink2Obj *) => %3
	pt + (%4 *) => Src2ObjX
	pt + (%5 *) => Src2ObjX
	pt + (%6 *) => Src2ObjX
	pt + (%7 *) => Sink2ObjX
	pt + (%8 *) => Sink2ObjX
	pt + (%9 *) => Sink2ObjX
	_pt + (Src2ObjT *) => %10
	_pt + (Src2ObjX *) => %11
	_pt + (Src2PrimFld[i] *) => %12[i]
	pt + (%13[i] *) => Src2PrimFld[i]
	pt + (%14 *) => Src2PrimFldArr
	_pt + (Sink2ObjT *) => %15
	_pt + (Sink2ObjX *) => %16
	_pt + (Sink2PrimFld[i] *) => %17[i]
	pt + (%18[i] *) => Sink2PrimFld[i]
	pt + (%19 *) => Sink2PrimFldArr
	_pt + (Src2Obj *) => LabelRef3
	_pt + (Sink2Obj *) => LabelRef3
sink2RefT:
	sink2RefT + (* pt) => Sink2ObjT
%1:
	%1 + (* pt) => SinkF2Obj
src2RefT:
	src2RefT + (* pt) => Src2ObjT
fpt:
	fpt + (Src2ObjT *) => Src2ObjT
	fpt + (Sink2ObjT *) => Sink2ObjT
	fpt + (SinkF2Obj *) => SinkF2Obj
sinkF2RefF:
	sinkF2RefF + (* pt) => SinkF2Obj
src2PrimT:
	src2PrimT => Src2Prim
Sink2PrimFld:
	Sink2PrimFld[i] + (* _pt) => %17[i]
sink2PrimT:
	sink2PrimT => Sink2Prim
storePrimCtxt:
	_storePrimCtxt[i] + (Src2Prim *) => %13[i]
	_storePrimCtxt[i] + (Sink2Prim *) => %18[i]
Src2Sink:
	Src2Sink => Flow3
%18:
	%18[i] + (* pt) => Sink2PrimFld[i]
loadStatPrimCtxt:
	_loadStatPrimCtxt[i] + (Src2PrimFldStat[i] *) => Src2Prim
	_loadStatPrimCtxt[i] + (Sink2PrimFldStat[i] *) => Sink2Prim
Sink2Obj:
	Sink2Obj + (* _pt) => %0
	Sink2Obj + (* _pt) => %3
	Sink2Obj + (* Obj2RefT) => %7
	Sink2Obj + (* Obj2PrimT) => Sink2Prim
	Sink2Obj + (* _pt) => LabelRef3
Src2PrimFld:
	Src2PrimFld + (* _SinkF2Obj) => Src2Sink
	Src2PrimFld[i] + (* _pt) => %12[i]
Sink2ObjT:
	Sink2ObjT + (* fpt) => Sink2ObjT
	Sink2ObjT => Sink2Obj
	Sink2ObjT + (* _pt) => %15
Obj2PrimT:
	Obj2PrimT + (_fptArr *) => Obj2PrimT
	Obj2PrimT + (Src2Obj *) => Src2Prim
	Obj2PrimT + (Src2PrimFldArr *) => Src2Prim
	Obj2PrimT + (Sink2Obj *) => Sink2Prim
	Obj2PrimT + (Sink2PrimFldArr *) => Sink2Prim
Sink2ObjX:
	Sink2ObjX => Sink2Obj
	Sink2ObjX + (* fptArr) => Sink2ObjX
	Sink2ObjX + (* _pt) => %16
%14:
	%14 + (* pt) => Src2PrimFldArr
Sink2PrimFldArr:
	Sink2PrimFldArr + (* Obj2RefT) => %9
	Sink2PrimFldArr + (* Obj2PrimT) => Sink2Prim
Obj2RefT:
	Obj2RefT + (_fptArr *) => Obj2RefT
	Obj2RefT + (Src2Obj *) => %4
	Obj2RefT + (Src2PrimFldArr *) => %6
	Obj2RefT + (Sink2Obj *) => %7
	Obj2RefT + (Sink2PrimFldArr *) => %9
%19:
	%19 + (* pt) => Sink2PrimFldArr
Src2ObjX:
	Src2ObjX => Src2Obj
	Src2ObjX + (* fptArr) => Src2ObjX
	Src2ObjX + (* _pt) => %11
SinkF2Prim:
	_SinkF2Prim + (Src2Prim *) => Src2Sink
Src2Prim:
	Src2Prim + (* _SinkF2Prim) => Src2Sink
	Src2Prim + (* prim2RefT) => %5
	Src2Prim + (* _assignPrimCtxt) => Src2Prim
	Src2Prim + (* _assignPrimCCtxt) => Src2Prim
	Src2Prim + (* prim2PrimT) => Src2Prim
	Src2Prim + (* _storePrimCtxt[i]) => %13[i]
	Src2Prim + (* _storePrimCtxtArr) => %14
	Src2Prim + (* _storeStatPrimCtxt[i]) => Src2PrimFldStat[i]
	Src2Prim => LabelPrim3
loadPrimCtxtArr:
	_loadPrimCtxtArr + (%11 *) => Src2Prim
	_loadPrimCtxtArr + (%16 *) => Sink2Prim
assignPrimCCtxt:
	_assignPrimCCtxt + (Src2Prim *) => Src2Prim
	_assignPrimCCtxt + (Sink2Prim *) => Sink2Prim
prim2PrimT:
	prim2PrimT + (Src2Prim *) => Src2Prim
	prim2PrimT + (Sink2Prim *) => Sink2Prim
%13:
	%13[i] + (* pt) => Src2PrimFld[i]
%12:
	%12[i] + (* _loadPrimCtxt[i]) => Src2Prim
%15:
	%15 + (* _loadPrimCtxt) => Sink2Prim
SinkF2Obj:
	SinkF2Obj + (* fpt) => SinkF2Obj
	_SinkF2Obj + (Src2Obj *) => Src2Sink
	_SinkF2Obj + (Src2PrimFld *) => Src2Sink
prim2PrimF:
	_prim2PrimF + (Sink2Prim *) => SinkF2Prim
%16:
	%16 + (* _loadPrimCtxtArr) => Sink2Prim
%11:
	%11 + (* _loadPrimCtxtArr) => Src2Prim
%10:
	%10 + (* _loadPrimCtxt) => Src2Prim
%17:
	%17[i] + (* _loadPrimCtxt[i]) => Sink2Prim
Src2Obj:
	Src2Obj + (* _SinkF2Obj) => Src2Sink
	Src2Obj + (* Obj2RefT) => %4
	Src2Obj + (* Obj2PrimT) => Src2Prim
	Src2Obj + (* _pt) => LabelRef3
%5:
	%5 + (* pt) => Src2ObjX
*/

public class G extends Graph {

public boolean isTerminal(int kind) {
  switch (kind) {
  case 1:
  case 2:
  case 4:
  case 5:
  case 7:
  case 8:
  case 10:
  case 12:
  case 14:
  case 18:
  case 21:
  case 22:
  case 24:
  case 31:
  case 40:
  case 41:
  case 42:
  case 43:
  case 44:
  case 46:
  case 50:
  case 51:
  case 53:
  case 55:
  case 56:
    return true;
  default:
    return false;
  }
}

public int numKinds() {
  return 67;
}

public int symbolToKind(String symbol) {
  if (symbol.equals("Obj2RefT")) return 0;
  if (symbol.equals("pt")) return 1;
  if (symbol.equals("ref2RefT")) return 2;
  if (symbol.equals("Obj2PrimT")) return 3;
  if (symbol.equals("ref2PrimT")) return 4;
  if (symbol.equals("fptArr")) return 5;
  if (symbol.equals("Src2ObjT")) return 6;
  if (symbol.equals("src2RefT")) return 7;
  if (symbol.equals("fpt")) return 8;
  if (symbol.equals("Sink2ObjT")) return 9;
  if (symbol.equals("sink2RefT")) return 10;
  if (symbol.equals("SinkF2Obj")) return 11;
  if (symbol.equals("sinkF2RefF")) return 12;
  if (symbol.equals("Sink2Obj")) return 13;
  if (symbol.equals("ref2RefF")) return 14;
  if (symbol.equals("%0")) return 15;
  if (symbol.equals("%1")) return 16;
  if (symbol.equals("Sink2Prim")) return 17;
  if (symbol.equals("ref2PrimF")) return 18;
  if (symbol.equals("%2")) return 19;
  if (symbol.equals("SinkF2Prim")) return 20;
  if (symbol.equals("sinkF2PrimF")) return 21;
  if (symbol.equals("prim2RefF")) return 22;
  if (symbol.equals("%3")) return 23;
  if (symbol.equals("prim2PrimF")) return 24;
  if (symbol.equals("Src2Sink")) return 25;
  if (symbol.equals("Src2Obj")) return 26;
  if (symbol.equals("Src2Prim")) return 27;
  if (symbol.equals("Src2PrimFld")) return 28;
  if (symbol.equals("Src2ObjX")) return 29;
  if (symbol.equals("%4")) return 30;
  if (symbol.equals("prim2RefT")) return 31;
  if (symbol.equals("%5")) return 32;
  if (symbol.equals("Src2PrimFldArr")) return 33;
  if (symbol.equals("%6")) return 34;
  if (symbol.equals("Sink2ObjX")) return 35;
  if (symbol.equals("%7")) return 36;
  if (symbol.equals("%8")) return 37;
  if (symbol.equals("Sink2PrimFldArr")) return 38;
  if (symbol.equals("%9")) return 39;
  if (symbol.equals("src2PrimT")) return 40;
  if (symbol.equals("assignPrimCtxt")) return 41;
  if (symbol.equals("assignPrimCCtxt")) return 42;
  if (symbol.equals("prim2PrimT")) return 43;
  if (symbol.equals("loadPrimCtxt")) return 44;
  if (symbol.equals("%10")) return 45;
  if (symbol.equals("loadPrimCtxtArr")) return 46;
  if (symbol.equals("%11")) return 47;
  if (symbol.equals("%12")) return 48;
  if (symbol.equals("Src2PrimFldStat")) return 49;
  if (symbol.equals("loadStatPrimCtxt")) return 50;
  if (symbol.equals("storePrimCtxt")) return 51;
  if (symbol.equals("%13")) return 52;
  if (symbol.equals("storePrimCtxtArr")) return 53;
  if (symbol.equals("%14")) return 54;
  if (symbol.equals("storeStatPrimCtxt")) return 55;
  if (symbol.equals("sink2PrimT")) return 56;
  if (symbol.equals("%15")) return 57;
  if (symbol.equals("%16")) return 58;
  if (symbol.equals("Sink2PrimFld")) return 59;
  if (symbol.equals("%17")) return 60;
  if (symbol.equals("Sink2PrimFldStat")) return 61;
  if (symbol.equals("%18")) return 62;
  if (symbol.equals("%19")) return 63;
  if (symbol.equals("LabelRef3")) return 64;
  if (symbol.equals("LabelPrim3")) return 65;
  if (symbol.equals("Flow3")) return 66;
  throw new RuntimeException("Unknown symbol "+symbol);
}

public String kindToSymbol(int kind) {
  switch (kind) {
  case 0: return "Obj2RefT";
  case 1: return "pt";
  case 2: return "ref2RefT";
  case 3: return "Obj2PrimT";
  case 4: return "ref2PrimT";
  case 5: return "fptArr";
  case 6: return "Src2ObjT";
  case 7: return "src2RefT";
  case 8: return "fpt";
  case 9: return "Sink2ObjT";
  case 10: return "sink2RefT";
  case 11: return "SinkF2Obj";
  case 12: return "sinkF2RefF";
  case 13: return "Sink2Obj";
  case 14: return "ref2RefF";
  case 15: return "%0";
  case 16: return "%1";
  case 17: return "Sink2Prim";
  case 18: return "ref2PrimF";
  case 19: return "%2";
  case 20: return "SinkF2Prim";
  case 21: return "sinkF2PrimF";
  case 22: return "prim2RefF";
  case 23: return "%3";
  case 24: return "prim2PrimF";
  case 25: return "Src2Sink";
  case 26: return "Src2Obj";
  case 27: return "Src2Prim";
  case 28: return "Src2PrimFld";
  case 29: return "Src2ObjX";
  case 30: return "%4";
  case 31: return "prim2RefT";
  case 32: return "%5";
  case 33: return "Src2PrimFldArr";
  case 34: return "%6";
  case 35: return "Sink2ObjX";
  case 36: return "%7";
  case 37: return "%8";
  case 38: return "Sink2PrimFldArr";
  case 39: return "%9";
  case 40: return "src2PrimT";
  case 41: return "assignPrimCtxt";
  case 42: return "assignPrimCCtxt";
  case 43: return "prim2PrimT";
  case 44: return "loadPrimCtxt";
  case 45: return "%10";
  case 46: return "loadPrimCtxtArr";
  case 47: return "%11";
  case 48: return "%12";
  case 49: return "Src2PrimFldStat";
  case 50: return "loadStatPrimCtxt";
  case 51: return "storePrimCtxt";
  case 52: return "%13";
  case 53: return "storePrimCtxtArr";
  case 54: return "%14";
  case 55: return "storeStatPrimCtxt";
  case 56: return "sink2PrimT";
  case 57: return "%15";
  case 58: return "%16";
  case 59: return "Sink2PrimFld";
  case 60: return "%17";
  case 61: return "Sink2PrimFldStat";
  case 62: return "%18";
  case 63: return "%19";
  case 64: return "LabelRef3";
  case 65: return "LabelPrim3";
  case 66: return "Flow3";
  default: throw new RuntimeException("Unknown kind "+kind);
  }
}

public void process(Edge base) {
  switch (base.kind) {
  case 0: /* Obj2RefT */
    /* Obj2RefT + (_fptArr *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(5)){
      addEdge(other.to, base.to, 0, base, other, false);
    }
    /* Obj2RefT + (Src2Obj *) => %4 */
    for(Edge other : base.from.getInEdges(26)){
      addEdge(other.from, base.to, 30, base, other, false);
    }
    /* Obj2RefT + (Src2PrimFldArr *) => %6 */
    for(Edge other : base.from.getInEdges(33)){
      addEdge(other.from, base.to, 34, base, other, false);
    }
    /* Obj2RefT + (Sink2Obj *) => %7 */
    for(Edge other : base.from.getInEdges(13)){
      addEdge(other.from, base.to, 36, base, other, false);
    }
    /* Obj2RefT + (Sink2PrimFldArr *) => %9 */
    for(Edge other : base.from.getInEdges(38)){
      addEdge(other.from, base.to, 39, base, other, false);
    }
    break;
  case 1: /* pt */
    /* _pt + (* ref2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(2)){
      addEdge(base.to, other.to, 0, base, other, false);
    }
    /* _pt + (* ref2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(4)){
      addEdge(base.to, other.to, 3, base, other, false);
    }
    /* pt + (src2RefT *) => Src2ObjT */
    for(Edge other : base.from.getInEdges(7)){
      addEdge(other.from, base.to, 6, base, other, false);
    }
    /* pt + (sink2RefT *) => Sink2ObjT */
    for(Edge other : base.from.getInEdges(10)){
      addEdge(other.from, base.to, 9, base, other, false);
    }
    /* pt + (sinkF2RefF *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(12)){
      addEdge(other.from, base.to, 11, base, other, false);
    }
    /* _pt + (Sink2Obj *) => %0 */
    for(Edge other : base.to.getInEdges(13)){
      addEdge(other.from, base.from, 15, base, other, false);
    }
    /* pt + (%1 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(16)){
      addEdge(other.from, base.to, 11, base, other, false);
    }
    /* pt + (%2 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(19)){
      addEdge(other.from, base.to, 11, base, other, false);
    }
    /* _pt + (Sink2Obj *) => %3 */
    for(Edge other : base.to.getInEdges(13)){
      addEdge(other.from, base.from, 23, base, other, false);
    }
    /* pt + (%4 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(30)){
      addEdge(other.from, base.to, 29, base, other, false);
    }
    /* pt + (%5 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(32)){
      addEdge(other.from, base.to, 29, base, other, false);
    }
    /* pt + (%6 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(34)){
      addEdge(other.from, base.to, 29, base, other, false);
    }
    /* pt + (%7 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(36)){
      addEdge(other.from, base.to, 35, base, other, false);
    }
    /* pt + (%8 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(37)){
      addEdge(other.from, base.to, 35, base, other, false);
    }
    /* pt + (%9 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(39)){
      addEdge(other.from, base.to, 35, base, other, false);
    }
    /* _pt + (Src2ObjT *) => %10 */
    for(Edge other : base.to.getInEdges(6)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    /* _pt + (Src2ObjX *) => %11 */
    for(Edge other : base.to.getInEdges(29)){
      addEdge(other.from, base.from, 47, base, other, false);
    }
    /* _pt + (Src2PrimFld[i] *) => %12[i] */
    for(Edge other : base.to.getInEdges(28)){
      addEdge(other.from, base.from, 48, base, other, true);
    }
    /* pt + (%13[i] *) => Src2PrimFld[i] */
    for(Edge other : base.from.getInEdges(52)){
      addEdge(other.from, base.to, 28, base, other, true);
    }
    /* pt + (%14 *) => Src2PrimFldArr */
    for(Edge other : base.from.getInEdges(54)){
      addEdge(other.from, base.to, 33, base, other, false);
    }
    /* _pt + (Sink2ObjT *) => %15 */
    for(Edge other : base.to.getInEdges(9)){
      addEdge(other.from, base.from, 57, base, other, false);
    }
    /* _pt + (Sink2ObjX *) => %16 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(other.from, base.from, 58, base, other, false);
    }
    /* _pt + (Sink2PrimFld[i] *) => %17[i] */
    for(Edge other : base.to.getInEdges(59)){
      addEdge(other.from, base.from, 60, base, other, true);
    }
    /* pt + (%18[i] *) => Sink2PrimFld[i] */
    for(Edge other : base.from.getInEdges(62)){
      addEdge(other.from, base.to, 59, base, other, true);
    }
    /* pt + (%19 *) => Sink2PrimFldArr */
    for(Edge other : base.from.getInEdges(63)){
      addEdge(other.from, base.to, 38, base, other, false);
    }
    /* _pt + (Src2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(26)){
      addEdge(other.from, base.from, 64, base, other, false);
    }
    /* _pt + (Sink2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(13)){
      addEdge(other.from, base.from, 64, base, other, false);
    }
    break;
  case 2: /* ref2RefT */
    /* ref2RefT + (_pt *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(1)){
      addEdge(other.to, base.to, 0, base, other, false);
    }
    break;
  case 3: /* Obj2PrimT */
    /* Obj2PrimT + (_fptArr *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(5)){
      addEdge(other.to, base.to, 3, base, other, false);
    }
    /* Obj2PrimT + (Src2Obj *) => Src2Prim */
    for(Edge other : base.from.getInEdges(26)){
      addEdge(other.from, base.to, 27, base, other, false);
    }
    /* Obj2PrimT + (Src2PrimFldArr *) => Src2Prim */
    for(Edge other : base.from.getInEdges(33)){
      addEdge(other.from, base.to, 27, base, other, false);
    }
    /* Obj2PrimT + (Sink2Obj *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(13)){
      addEdge(other.from, base.to, 17, base, other, false);
    }
    /* Obj2PrimT + (Sink2PrimFldArr *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(38)){
      addEdge(other.from, base.to, 17, base, other, false);
    }
    break;
  case 4: /* ref2PrimT */
    /* ref2PrimT + (_pt *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(1)){
      addEdge(other.to, base.to, 3, base, other, false);
    }
    break;
  case 5: /* fptArr */
    /* _fptArr + (* Obj2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(0)){
      addEdge(base.to, other.to, 0, base, other, false);
    }
    /* _fptArr + (* Obj2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(3)){
      addEdge(base.to, other.to, 3, base, other, false);
    }
    /* fptArr + (Src2ObjX *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(29)){
      addEdge(other.from, base.to, 29, base, other, false);
    }
    /* fptArr + (Sink2ObjX *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(35)){
      addEdge(other.from, base.to, 35, base, other, false);
    }
    break;
  case 6: /* Src2ObjT */
    /* Src2ObjT + (* fpt) => Src2ObjT */
    for(Edge other : base.to.getOutEdges(8)){
      addEdge(base.from, other.to, 6, base, other, false);
    }
    /* Src2ObjT => Src2Obj */
    addEdge(base.from, base.to, 26, base, false);
    /* Src2ObjT + (* _pt) => %10 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    break;
  case 7: /* src2RefT */
    /* src2RefT + (* pt) => Src2ObjT */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 6, base, other, false);
    }
    break;
  case 8: /* fpt */
    /* fpt + (Src2ObjT *) => Src2ObjT */
    for(Edge other : base.from.getInEdges(6)){
      addEdge(other.from, base.to, 6, base, other, false);
    }
    /* fpt + (Sink2ObjT *) => Sink2ObjT */
    for(Edge other : base.from.getInEdges(9)){
      addEdge(other.from, base.to, 9, base, other, false);
    }
    /* fpt + (SinkF2Obj *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(11)){
      addEdge(other.from, base.to, 11, base, other, false);
    }
    break;
  case 9: /* Sink2ObjT */
    /* Sink2ObjT + (* fpt) => Sink2ObjT */
    for(Edge other : base.to.getOutEdges(8)){
      addEdge(base.from, other.to, 9, base, other, false);
    }
    /* Sink2ObjT => Sink2Obj */
    addEdge(base.from, base.to, 13, base, false);
    /* Sink2ObjT + (* _pt) => %15 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 57, base, other, false);
    }
    break;
  case 10: /* sink2RefT */
    /* sink2RefT + (* pt) => Sink2ObjT */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 9, base, other, false);
    }
    break;
  case 11: /* SinkF2Obj */
    /* SinkF2Obj + (* fpt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(8)){
      addEdge(base.from, other.to, 11, base, other, false);
    }
    /* _SinkF2Obj + (Src2Obj *) => Src2Sink */
    for(Edge other : base.to.getInEdges(26)){
      addEdge(other.from, base.from, 25, base, other, false);
    }
    /* _SinkF2Obj + (Src2PrimFld *) => Src2Sink */
    for(Edge other : base.to.getInEdges(28)){
      addEdge(other.from, base.from, 25, base, other, false);
    }
    break;
  case 12: /* sinkF2RefF */
    /* sinkF2RefF + (* pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 11, base, other, false);
    }
    break;
  case 13: /* Sink2Obj */
    /* Sink2Obj + (* _pt) => %0 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 15, base, other, false);
    }
    /* Sink2Obj + (* _pt) => %3 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 23, base, other, false);
    }
    /* Sink2Obj + (* Obj2RefT) => %7 */
    for(Edge other : base.to.getOutEdges(0)){
      addEdge(base.from, other.to, 36, base, other, false);
    }
    /* Sink2Obj + (* Obj2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(3)){
      addEdge(base.from, other.to, 17, base, other, false);
    }
    /* Sink2Obj + (* _pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 64, base, other, false);
    }
    break;
  case 14: /* ref2RefF */
    /* _ref2RefF + (%0 *) => %1 */
    for(Edge other : base.to.getInEdges(15)){
      addEdge(other.from, base.from, 16, base, other, false);
    }
    break;
  case 15: /* %0 */
    /* %0 + (* _ref2RefF) => %1 */
    for(Edge other : base.to.getInEdges(14)){
      addEdge(base.from, other.from, 16, base, other, false);
    }
    break;
  case 16: /* %1 */
    /* %1 + (* pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 11, base, other, false);
    }
    break;
  case 17: /* Sink2Prim */
    /* Sink2Prim + (* _ref2PrimF) => %2 */
    for(Edge other : base.to.getInEdges(18)){
      addEdge(base.from, other.from, 19, base, other, false);
    }
    /* Sink2Prim + (* _prim2PrimF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(24)){
      addEdge(base.from, other.from, 20, base, other, false);
    }
    /* Sink2Prim + (* prim2RefT) => %8 */
    for(Edge other : base.to.getOutEdges(31)){
      addEdge(base.from, other.to, 37, base, other, false);
    }
    /* Sink2Prim + (* _assignPrimCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(41)){
      addEdge(base.from, other.from, 17, base, other, false);
    }
    /* Sink2Prim + (* _assignPrimCCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 17, base, other, false);
    }
    /* Sink2Prim + (* prim2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(43)){
      addEdge(base.from, other.to, 17, base, other, false);
    }
    /* Sink2Prim + (* _storePrimCtxt[i]) => %18[i] */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(base.from, other.from, 62, base, other, true);
    }
    /* Sink2Prim + (* _storePrimCtxtArr) => %19 */
    for(Edge other : base.to.getInEdges(53)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    /* Sink2Prim + (* _storeStatPrimCtxt[i]) => Sink2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(55)){
      addEdge(base.from, other.from, 61, base, other, true);
    }
    /* Sink2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 65, base, false);
    break;
  case 18: /* ref2PrimF */
    /* _ref2PrimF + (Sink2Prim *) => %2 */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 19, base, other, false);
    }
    break;
  case 19: /* %2 */
    /* %2 + (* pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 11, base, other, false);
    }
    break;
  case 20: /* SinkF2Prim */
    /* _SinkF2Prim + (Src2Prim *) => Src2Sink */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(other.from, base.from, 25, base, other, false);
    }
    break;
  case 21: /* sinkF2PrimF */
    /* sinkF2PrimF => SinkF2Prim */
    addEdge(base.from, base.to, 20, base, false);
    break;
  case 22: /* prim2RefF */
    /* _prim2RefF + (%3 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(23)){
      addEdge(other.from, base.from, 20, base, other, false);
    }
    break;
  case 23: /* %3 */
    /* %3 + (* _prim2RefF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(22)){
      addEdge(base.from, other.from, 20, base, other, false);
    }
    break;
  case 24: /* prim2PrimF */
    /* _prim2PrimF + (Sink2Prim *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 20, base, other, false);
    }
    break;
  case 25: /* Src2Sink */
    /* Src2Sink => Flow3 */
    addEdge(base.from, base.to, 66, base, false);
    break;
  case 26: /* Src2Obj */
    /* Src2Obj + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(11)){
      addEdge(base.from, other.from, 25, base, other, false);
    }
    /* Src2Obj + (* Obj2RefT) => %4 */
    for(Edge other : base.to.getOutEdges(0)){
      addEdge(base.from, other.to, 30, base, other, false);
    }
    /* Src2Obj + (* Obj2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(3)){
      addEdge(base.from, other.to, 27, base, other, false);
    }
    /* Src2Obj + (* _pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 64, base, other, false);
    }
    break;
  case 27: /* Src2Prim */
    /* Src2Prim + (* _SinkF2Prim) => Src2Sink */
    for(Edge other : base.to.getInEdges(20)){
      addEdge(base.from, other.from, 25, base, other, false);
    }
    /* Src2Prim + (* prim2RefT) => %5 */
    for(Edge other : base.to.getOutEdges(31)){
      addEdge(base.from, other.to, 32, base, other, false);
    }
    /* Src2Prim + (* _assignPrimCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(41)){
      addEdge(base.from, other.from, 27, base, other, false);
    }
    /* Src2Prim + (* _assignPrimCCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 27, base, other, false);
    }
    /* Src2Prim + (* prim2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(43)){
      addEdge(base.from, other.to, 27, base, other, false);
    }
    /* Src2Prim + (* _storePrimCtxt[i]) => %13[i] */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(base.from, other.from, 52, base, other, true);
    }
    /* Src2Prim + (* _storePrimCtxtArr) => %14 */
    for(Edge other : base.to.getInEdges(53)){
      addEdge(base.from, other.from, 54, base, other, false);
    }
    /* Src2Prim + (* _storeStatPrimCtxt[i]) => Src2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(55)){
      addEdge(base.from, other.from, 49, base, other, true);
    }
    /* Src2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 65, base, false);
    break;
  case 28: /* Src2PrimFld */
    /* Src2PrimFld + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(11)){
      addEdge(base.from, other.from, 25, base, other, false);
    }
    /* Src2PrimFld[i] + (* _pt) => %12[i] */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 48, base, other, true);
    }
    break;
  case 29: /* Src2ObjX */
    /* Src2ObjX => Src2Obj */
    addEdge(base.from, base.to, 26, base, false);
    /* Src2ObjX + (* fptArr) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(5)){
      addEdge(base.from, other.to, 29, base, other, false);
    }
    /* Src2ObjX + (* _pt) => %11 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 47, base, other, false);
    }
    break;
  case 30: /* %4 */
    /* %4 + (* pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 29, base, other, false);
    }
    break;
  case 31: /* prim2RefT */
    /* prim2RefT + (Src2Prim *) => %5 */
    for(Edge other : base.from.getInEdges(27)){
      addEdge(other.from, base.to, 32, base, other, false);
    }
    /* prim2RefT + (Sink2Prim *) => %8 */
    for(Edge other : base.from.getInEdges(17)){
      addEdge(other.from, base.to, 37, base, other, false);
    }
    break;
  case 32: /* %5 */
    /* %5 + (* pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 29, base, other, false);
    }
    break;
  case 33: /* Src2PrimFldArr */
    /* Src2PrimFldArr + (* Obj2RefT) => %6 */
    for(Edge other : base.to.getOutEdges(0)){
      addEdge(base.from, other.to, 34, base, other, false);
    }
    /* Src2PrimFldArr + (* Obj2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(3)){
      addEdge(base.from, other.to, 27, base, other, false);
    }
    break;
  case 34: /* %6 */
    /* %6 + (* pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 29, base, other, false);
    }
    break;
  case 35: /* Sink2ObjX */
    /* Sink2ObjX => Sink2Obj */
    addEdge(base.from, base.to, 13, base, false);
    /* Sink2ObjX + (* fptArr) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(5)){
      addEdge(base.from, other.to, 35, base, other, false);
    }
    /* Sink2ObjX + (* _pt) => %16 */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 58, base, other, false);
    }
    break;
  case 36: /* %7 */
    /* %7 + (* pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 35, base, other, false);
    }
    break;
  case 37: /* %8 */
    /* %8 + (* pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 35, base, other, false);
    }
    break;
  case 38: /* Sink2PrimFldArr */
    /* Sink2PrimFldArr + (* Obj2RefT) => %9 */
    for(Edge other : base.to.getOutEdges(0)){
      addEdge(base.from, other.to, 39, base, other, false);
    }
    /* Sink2PrimFldArr + (* Obj2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(3)){
      addEdge(base.from, other.to, 17, base, other, false);
    }
    break;
  case 39: /* %9 */
    /* %9 + (* pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 35, base, other, false);
    }
    break;
  case 40: /* src2PrimT */
    /* src2PrimT => Src2Prim */
    addEdge(base.from, base.to, 27, base, false);
    break;
  case 41: /* assignPrimCtxt */
    /* _assignPrimCtxt + (Src2Prim *) => Src2Prim */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(other.from, base.from, 27, base, other, false);
    }
    /* _assignPrimCtxt + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 17, base, other, false);
    }
    break;
  case 42: /* assignPrimCCtxt */
    /* _assignPrimCCtxt + (Src2Prim *) => Src2Prim */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(other.from, base.from, 27, base, other, false);
    }
    /* _assignPrimCCtxt + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 17, base, other, false);
    }
    break;
  case 43: /* prim2PrimT */
    /* prim2PrimT + (Src2Prim *) => Src2Prim */
    for(Edge other : base.from.getInEdges(27)){
      addEdge(other.from, base.to, 27, base, other, false);
    }
    /* prim2PrimT + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(17)){
      addEdge(other.from, base.to, 17, base, other, false);
    }
    break;
  case 44: /* loadPrimCtxt */
    /* _loadPrimCtxt + (%10 *) => Src2Prim */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 27, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%12[i] *) => Src2Prim */
    for(Edge other : base.to.getInEdges(48)){
      addEdge(other.from, base.from, 27, base, other, false);
    }
    /* _loadPrimCtxt + (%15 *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(57)){
      addEdge(other.from, base.from, 17, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%17[i] *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 17, base, other, false);
    }
    break;
  case 45: /* %10 */
    /* %10 + (* _loadPrimCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(44)){
      addEdge(base.from, other.from, 27, base, other, false);
    }
    break;
  case 46: /* loadPrimCtxtArr */
    /* _loadPrimCtxtArr + (%11 *) => Src2Prim */
    for(Edge other : base.to.getInEdges(47)){
      addEdge(other.from, base.from, 27, base, other, false);
    }
    /* _loadPrimCtxtArr + (%16 *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 17, base, other, false);
    }
    break;
  case 47: /* %11 */
    /* %11 + (* _loadPrimCtxtArr) => Src2Prim */
    for(Edge other : base.to.getInEdges(46)){
      addEdge(base.from, other.from, 27, base, other, false);
    }
    break;
  case 48: /* %12 */
    /* %12[i] + (* _loadPrimCtxt[i]) => Src2Prim */
    for(Edge other : base.to.getInEdges(44)){
      addEdge(base.from, other.from, 27, base, other, false);
    }
    break;
  case 49: /* Src2PrimFldStat */
    /* Src2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Src2Prim */
    for(Edge other : base.to.getInEdges(50)){
      addEdge(base.from, other.from, 27, base, other, false);
    }
    break;
  case 50: /* loadStatPrimCtxt */
    /* _loadStatPrimCtxt[i] + (Src2PrimFldStat[i] *) => Src2Prim */
    for(Edge other : base.to.getInEdges(49)){
      addEdge(other.from, base.from, 27, base, other, false);
    }
    /* _loadStatPrimCtxt[i] + (Sink2PrimFldStat[i] *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 17, base, other, false);
    }
    break;
  case 51: /* storePrimCtxt */
    /* _storePrimCtxt[i] + (Src2Prim *) => %13[i] */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(other.from, base.from, 52, base, other, true);
    }
    /* _storePrimCtxt[i] + (Sink2Prim *) => %18[i] */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 62, base, other, true);
    }
    break;
  case 52: /* %13 */
    /* %13[i] + (* pt) => Src2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 28, base, other, true);
    }
    break;
  case 53: /* storePrimCtxtArr */
    /* _storePrimCtxtArr + (Src2Prim *) => %14 */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(other.from, base.from, 54, base, other, false);
    }
    /* _storePrimCtxtArr + (Sink2Prim *) => %19 */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 54: /* %14 */
    /* %14 + (* pt) => Src2PrimFldArr */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 33, base, other, false);
    }
    break;
  case 55: /* storeStatPrimCtxt */
    /* _storeStatPrimCtxt[i] + (Src2Prim *) => Src2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(27)){
      addEdge(other.from, base.from, 49, base, other, true);
    }
    /* _storeStatPrimCtxt[i] + (Sink2Prim *) => Sink2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(17)){
      addEdge(other.from, base.from, 61, base, other, true);
    }
    break;
  case 56: /* sink2PrimT */
    /* sink2PrimT => Sink2Prim */
    addEdge(base.from, base.to, 17, base, false);
    break;
  case 57: /* %15 */
    /* %15 + (* _loadPrimCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(44)){
      addEdge(base.from, other.from, 17, base, other, false);
    }
    break;
  case 58: /* %16 */
    /* %16 + (* _loadPrimCtxtArr) => Sink2Prim */
    for(Edge other : base.to.getInEdges(46)){
      addEdge(base.from, other.from, 17, base, other, false);
    }
    break;
  case 59: /* Sink2PrimFld */
    /* Sink2PrimFld[i] + (* _pt) => %17[i] */
    for(Edge other : base.to.getInEdges(1)){
      addEdge(base.from, other.from, 60, base, other, true);
    }
    break;
  case 60: /* %17 */
    /* %17[i] + (* _loadPrimCtxt[i]) => Sink2Prim */
    for(Edge other : base.to.getInEdges(44)){
      addEdge(base.from, other.from, 17, base, other, false);
    }
    break;
  case 61: /* Sink2PrimFldStat */
    /* Sink2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Sink2Prim */
    for(Edge other : base.to.getInEdges(50)){
      addEdge(base.from, other.from, 17, base, other, false);
    }
    break;
  case 62: /* %18 */
    /* %18[i] + (* pt) => Sink2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 59, base, other, true);
    }
    break;
  case 63: /* %19 */
    /* %19 + (* pt) => Sink2PrimFldArr */
    for(Edge other : base.to.getOutEdges(1)){
      addEdge(base.from, other.to, 38, base, other, false);
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