package stamp.missingmodels.grammars;
import stamp.missingmodels.util.jcflsolver.*;

/* Original Grammar:
###################
# CONFIGURATION
###################

.output LabelRef3
.output LabelPrim3
.output Flow3

.weights ref2RefArgTStub 1
.weights ref2RefRetTStub 1

.weights prim2RefArgTStub 1
.weights prim2RefRetTStub 1

.weights ref2PrimTStub 1

.weights prim2PrimT 1

.weights transfer 1

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
# RULES: ANNOTATION CONVERSION
###################

# transfer annotations

Ref2RefT :: ref2RefT
Ref2PrimT :: ref2PrimT
Prim2RefT :: prim2RefT
Prim2PrimT :: prim2PrimT

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
# RULES: STUB ANNOTATION CONVERSION
###################

Ref2RefT :: ref2RefArgTStub
Ref2RefT :: ref2RefRetTStub

Prim2RefT :: prim2RefArgTStub
Prim2RefT :: prim2RefRetTStub

Ref2PrimT :: ref2PrimTStub
Prim2PrimT :: prim2PrimTStub

###################
# RULES: PARTIAL FLOW PROPAGATION
###################

PreFlowsTo :: preFlowsTo
PostFlowsTo :: postFlowsTo
MidFlowsTo :: midFlowsTo

Transfer :: transfer
Transfer :: transferSelf

PreFlowsTo :: PreFlowsTo Transfer MidFlowsTo

Pt :: _PostFlowsTo _Transfer _PreFlowsTo

Fpt[f] :: _Pt Store[f] Pt
FptArr :: _Pt StoreArr Pt

Pt :: pt

Fpt[f] :: fpt[f]
FptArr :: fptArr

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

###################
# RULES: OUTPUT
###################

LabelRef3 :: Label2Obj _Pt
LabelPrim3 :: Label2Prim
Flow3 :: Src2Sink
*/

/* Normalized Grammar:
Label2Obj:
	Label2Obj :: Label2ObjT
	Label2Obj :: Label2ObjX
PostFlowsTo:
	PostFlowsTo :: postFlowsTo
Pt:
	Pt :: %1 _PreFlowsTo
	Pt :: pt
Transfer:
	Transfer :: transfer
	Transfer :: transferSelf
Flow3:
	Flow3 :: Src2Sink
%14:
	%14 :: src2Label Label2PrimFld
Fpt:
	Fpt[i] :: %2[i] Pt
	Fpt[i] :: fpt[i]
%9:
	%9 :: sink2Label Label2Obj
%8:
	%8 :: %7 _Ref2PrimF
%5:
	%5 :: %4 _Pt
%4:
	%4 :: sink2Label Label2Obj
%7:
	%7 :: sink2Label Label2Prim
%6:
	%6 :: %5 _Ref2RefF
%1:
	%1 :: _PostFlowsTo _Transfer
%0:
	%0 :: PreFlowsTo Transfer
%3:
	%3 :: _Pt StoreArr
%2:
	%2[i] :: _Pt Store[i]
%20:
	%20[i] :: Label2PrimFld[i] _Pt
%21:
	%21[i] :: Label2Prim _storePrimCtxt[i]
%22:
	%22 :: Label2Prim _storePrimCtxtArr
Label2RefT:
	Label2RefT :: label2RefT
FptArr:
	FptArr :: %3 Pt
	FptArr :: fptArr
Label2PrimFldStat:
	Label2PrimFldStat[i] :: Label2Prim _storeStatPrimCtxt[i]
SinkF2PrimF:
	SinkF2PrimF :: sinkF2PrimF
Label2ObjT:
	Label2ObjT :: Label2RefT Pt
	Label2ObjT :: Label2ObjT Fpt
PreFlowsTo:
	PreFlowsTo :: preFlowsTo
	PreFlowsTo :: %0 MidFlowsTo
Label2ObjX:
	Label2ObjX :: %15 Pt
	Label2ObjX :: %16 Pt
	Label2ObjX :: %17 Pt
	Label2ObjX :: Label2ObjX FptArr
Label2PrimFld:
	Label2PrimFld[i] :: %21[i] Pt
Src2Sink:
	Src2Sink :: %12 _SinkF2Obj
	Src2Sink :: %13 _SinkF2Prim
	Src2Sink :: %14 _SinkF2Obj
Label2PrimFldArr:
	Label2PrimFldArr :: %22 Pt
Prim2PrimF:
	Prim2PrimF :: prim2PrimF
LabelPrim3:
	LabelPrim3 :: Label2Prim
SinkF2RefF:
	SinkF2RefF :: sinkF2RefF
%19:
	%19 :: Label2ObjX _Pt
Obj2PrimT:
	Obj2PrimT :: _Pt Ref2PrimT
	Obj2PrimT :: _FptArr Obj2PrimT
Prim2PrimT:
	Prim2PrimT :: prim2PrimT
	Prim2PrimT :: prim2PrimTStub
Label2Prim:
	Label2Prim :: Label2PrimT
	Label2Prim :: Label2Prim _assignPrimCtxt
	Label2Prim :: Label2Prim _assignPrimCCtxt
	Label2Prim :: Label2Obj Obj2PrimT
	Label2Prim :: Label2Prim Prim2PrimT
	Label2Prim :: %18 _loadPrimCtxt
	Label2Prim :: %19 _loadPrimCtxtArr
	Label2Prim :: Label2PrimFldArr Obj2PrimT
	Label2Prim :: %20[i] _loadPrimCtxt[i]
	Label2Prim :: Label2PrimFldStat[i] _loadStatPrimCtxt[i]
Obj2RefT:
	Obj2RefT :: _Pt Ref2RefT
	Obj2RefT :: _FptArr Obj2RefT
Ref2PrimT:
	Ref2PrimT :: ref2PrimT
	Ref2PrimT :: ref2PrimTStub
%18:
	%18 :: Label2ObjT _Pt
SinkF2Prim:
	SinkF2Prim :: SinkF2PrimF
	SinkF2Prim :: %10 _Prim2RefF
	SinkF2Prim :: %11 _Prim2PrimF
Ref2RefF:
	Ref2RefF :: ref2RefF
%11:
	%11 :: sink2Label Label2Prim
%10:
	%10 :: %9 _Pt
Prim2RefT:
	Prim2RefT :: prim2RefT
	Prim2RefT :: prim2RefArgTStub
	Prim2RefT :: prim2RefRetTStub
%12:
	%12 :: src2Label Label2Obj
%15:
	%15 :: Label2Obj Obj2RefT
SinkF2Obj:
	SinkF2Obj :: SinkF2RefF Pt
	SinkF2Obj :: %6 Pt
	SinkF2Obj :: %8 Pt
	SinkF2Obj :: SinkF2Obj Fpt
%17:
	%17 :: Label2PrimFldArr Obj2RefT
%16:
	%16 :: Label2Prim Prim2RefT
Ref2PrimF:
	Ref2PrimF :: ref2PrimF
Label2PrimT:
	Label2PrimT :: label2PrimT
Ref2RefT:
	Ref2RefT :: ref2RefT
	Ref2RefT :: ref2RefArgTStub
	Ref2RefT :: ref2RefRetTStub
Prim2RefF:
	Prim2RefF :: prim2RefF
MidFlowsTo:
	MidFlowsTo :: midFlowsTo
%13:
	%13 :: src2Label Label2Prim
LabelRef3:
	LabelRef3 :: Label2Obj _Pt
*/

/* Reverse Productions:
storePrimCtxtArr:
	_storePrimCtxtArr + (Label2Prim *) => %22
prim2RefT:
	prim2RefT => Prim2RefT
%9:
	%9 + (* _Pt) => %10
%8:
	%8 + (* Pt) => SinkF2Obj
prim2RefF:
	prim2RefF => Prim2RefF
%4:
	%4 + (* _Pt) => %5
%7:
	%7 + (* _Ref2PrimF) => %8
%6:
	%6 + (* Pt) => SinkF2Obj
%1:
	%1 + (* _PreFlowsTo) => Pt
%0:
	%0 + (* MidFlowsTo) => PreFlowsTo
%3:
	%3 + (* Pt) => FptArr
%2:
	%2[i] + (* Pt) => Fpt[i]
postFlowsTo:
	postFlowsTo => PostFlowsTo
Label2ObjT:
	Label2ObjT + (* Fpt) => Label2ObjT
	Label2ObjT => Label2Obj
	Label2ObjT + (* _Pt) => %18
Label2ObjX:
	Label2ObjX => Label2Obj
	Label2ObjX + (* FptArr) => Label2ObjX
	Label2ObjX + (* _Pt) => %19
label2RefT:
	label2RefT => Label2RefT
storePrimCtxt:
	_storePrimCtxt[i] + (Label2Prim *) => %21[i]
MidFlowsTo:
	MidFlowsTo + (%0 *) => PreFlowsTo
preFlowsTo:
	preFlowsTo => PreFlowsTo
Ref2RefF:
	_Ref2RefF + (%5 *) => %6
assignPrimCCtxt:
	_assignPrimCCtxt + (Label2Prim *) => Label2Prim
SinkF2Obj:
	SinkF2Obj + (* Fpt) => SinkF2Obj
	_SinkF2Obj + (%12 *) => Src2Sink
	_SinkF2Obj + (%14 *) => Src2Sink
Label2Prim:
	Label2Prim + (sink2Label *) => %7
	Label2Prim + (sink2Label *) => %11
	Label2Prim + (src2Label *) => %13
	Label2Prim + (* Prim2RefT) => %16
	Label2Prim + (* _assignPrimCtxt) => Label2Prim
	Label2Prim + (* _assignPrimCCtxt) => Label2Prim
	Label2Prim + (* Prim2PrimT) => Label2Prim
	Label2Prim + (* _storePrimCtxt[i]) => %21[i]
	Label2Prim + (* _storePrimCtxtArr) => %22
	Label2Prim + (* _storeStatPrimCtxt[i]) => Label2PrimFldStat[i]
	Label2Prim => LabelPrim3
Label2PrimT:
	Label2PrimT => Label2Prim
Ref2RefT:
	Ref2RefT + (_Pt *) => Obj2RefT
StoreArr:
	StoreArr + (_Pt *) => %3
ref2PrimT:
	ref2PrimT => Ref2PrimT
Pt:
	_Pt + (* Store[i]) => %2[i]
	Pt + (%2[i] *) => Fpt[i]
	_Pt + (* StoreArr) => %3
	Pt + (%3 *) => FptArr
	_Pt + (* Ref2RefT) => Obj2RefT
	_Pt + (* Ref2PrimT) => Obj2PrimT
	Pt + (Label2RefT *) => Label2ObjT
	Pt + (SinkF2RefF *) => SinkF2Obj
	_Pt + (%4 *) => %5
	Pt + (%6 *) => SinkF2Obj
	Pt + (%8 *) => SinkF2Obj
	_Pt + (%9 *) => %10
	Pt + (%15 *) => Label2ObjX
	Pt + (%16 *) => Label2ObjX
	Pt + (%17 *) => Label2ObjX
	_Pt + (Label2ObjT *) => %18
	_Pt + (Label2ObjX *) => %19
	_Pt + (Label2PrimFld[i] *) => %20[i]
	Pt + (%21[i] *) => Label2PrimFld[i]
	Pt + (%22 *) => Label2PrimFldArr
	_Pt + (Label2Obj *) => LabelRef3
prim2RefArgTStub:
	prim2RefArgTStub => Prim2RefT
%5:
	%5 + (* _Ref2RefF) => %6
ref2PrimF:
	ref2PrimF => Ref2PrimF
Label2PrimFldArr:
	Label2PrimFldArr + (* Obj2RefT) => %17
	Label2PrimFldArr + (* Obj2PrimT) => Label2Prim
FptArr:
	_FptArr + (* Obj2RefT) => Obj2RefT
	_FptArr + (* Obj2PrimT) => Obj2PrimT
	FptArr + (Label2ObjX *) => Label2ObjX
SinkF2PrimF:
	SinkF2PrimF => SinkF2Prim
Src2Sink:
	Src2Sink => Flow3
loadStatPrimCtxt:
	_loadStatPrimCtxt[i] + (Label2PrimFldStat[i] *) => Label2Prim
Prim2PrimF:
	_Prim2PrimF + (%11 *) => SinkF2Prim
Prim2PrimT:
	Prim2PrimT + (Label2Prim *) => Label2Prim
storeStatPrimCtxt:
	_storeStatPrimCtxt[i] + (Label2Prim *) => Label2PrimFldStat[i]
Ref2PrimT:
	Ref2PrimT + (_Pt *) => Obj2PrimT
SinkF2Prim:
	_SinkF2Prim + (%13 *) => Src2Sink
loadPrimCtxtArr:
	_loadPrimCtxtArr + (%19 *) => Label2Prim
Ref2PrimF:
	_Ref2PrimF + (%7 *) => %8
sink2Label:
	sink2Label + (* Label2Obj) => %4
	sink2Label + (* Label2Prim) => %7
	sink2Label + (* Label2Obj) => %9
	sink2Label + (* Label2Prim) => %11
PostFlowsTo:
	_PostFlowsTo + (* _Transfer) => %1
assignPrimCtxt:
	_assignPrimCtxt + (Label2Prim *) => Label2Prim
fptArr:
	fptArr => FptArr
label2PrimT:
	label2PrimT => Label2PrimT
sinkF2PrimF:
	sinkF2PrimF => SinkF2PrimF
Store:
	Store[i] + (_Pt *) => %2[i]
pt:
	pt => Pt
Label2RefT:
	Label2RefT + (* Pt) => Label2ObjT
transfer:
	transfer => Transfer
transferSelf:
	transferSelf => Transfer
sinkF2RefF:
	sinkF2RefF => SinkF2RefF
Label2PrimFldStat:
	Label2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Label2Prim
ref2PrimTStub:
	ref2PrimTStub => Ref2PrimT
src2Label:
	src2Label + (* Label2Obj) => %12
	src2Label + (* Label2Prim) => %13
	src2Label + (* Label2PrimFld) => %14
Obj2PrimT:
	Obj2PrimT + (_FptArr *) => Obj2PrimT
	Obj2PrimT + (Label2Obj *) => Label2Prim
	Obj2PrimT + (Label2PrimFldArr *) => Label2Prim
loadPrimCtxt:
	_loadPrimCtxt + (%18 *) => Label2Prim
	_loadPrimCtxt[i] + (%20[i] *) => Label2Prim
Obj2RefT:
	Obj2RefT + (_FptArr *) => Obj2RefT
	Obj2RefT + (Label2Obj *) => %15
	Obj2RefT + (Label2PrimFldArr *) => %17
Prim2RefT:
	Prim2RefT + (Label2Prim *) => %16
prim2PrimF:
	prim2PrimF => Prim2PrimF
midFlowsTo:
	midFlowsTo => MidFlowsTo
Prim2RefF:
	_Prim2RefF + (%10 *) => SinkF2Prim
prim2PrimT:
	prim2PrimT => Prim2PrimT
Label2Obj:
	Label2Obj + (sink2Label *) => %4
	Label2Obj + (sink2Label *) => %9
	Label2Obj + (src2Label *) => %12
	Label2Obj + (* Obj2RefT) => %15
	Label2Obj + (* Obj2PrimT) => Label2Prim
	Label2Obj + (* _Pt) => LabelRef3
ref2RefF:
	ref2RefF => Ref2RefF
Transfer:
	Transfer + (PreFlowsTo *) => %0
	_Transfer + (_PostFlowsTo *) => %1
Fpt:
	Fpt + (Label2ObjT *) => Label2ObjT
	Fpt + (SinkF2Obj *) => SinkF2Obj
ref2RefArgTStub:
	ref2RefArgTStub => Ref2RefT
ref2RefT:
	ref2RefT => Ref2RefT
Label2PrimFld:
	Label2PrimFld + (src2Label *) => %14
	Label2PrimFld[i] + (* _Pt) => %20[i]
%20:
	%20[i] + (* _loadPrimCtxt[i]) => Label2Prim
%21:
	%21[i] + (* Pt) => Label2PrimFld[i]
%22:
	%22 + (* Pt) => Label2PrimFldArr
prim2PrimTStub:
	prim2PrimTStub => Prim2PrimT
fpt:
	fpt[i] => Fpt[i]
PreFlowsTo:
	PreFlowsTo + (* Transfer) => %0
	_PreFlowsTo + (%1 *) => Pt
ref2RefRetTStub:
	ref2RefRetTStub => Ref2RefT
SinkF2RefF:
	SinkF2RefF + (* Pt) => SinkF2Obj
prim2RefRetTStub:
	prim2RefRetTStub => Prim2RefT
%19:
	%19 + (* _loadPrimCtxtArr) => Label2Prim
%18:
	%18 + (* _loadPrimCtxt) => Label2Prim
%11:
	%11 + (* _Prim2PrimF) => SinkF2Prim
%10:
	%10 + (* _Prim2RefF) => SinkF2Prim
%13:
	%13 + (* _SinkF2Prim) => Src2Sink
%12:
	%12 + (* _SinkF2Obj) => Src2Sink
%15:
	%15 + (* Pt) => Label2ObjX
%14:
	%14 + (* _SinkF2Obj) => Src2Sink
%17:
	%17 + (* Pt) => Label2ObjX
%16:
	%16 + (* Pt) => Label2ObjX
*/

public class G extends Graph {

public boolean isTerminal(int kind) {
  switch (kind) {
  case 1:
  case 3:
  case 5:
  case 7:
  case 9:
  case 11:
  case 13:
  case 15:
  case 17:
  case 19:
  case 21:
  case 23:
  case 24:
  case 25:
  case 26:
  case 27:
  case 28:
  case 29:
  case 31:
  case 33:
  case 35:
  case 37:
  case 38:
  case 48:
  case 49:
  case 50:
  case 55:
  case 68:
  case 78:
  case 79:
  case 80:
  case 82:
  case 86:
  case 87:
  case 89:
  case 91:
    return true;
  default:
    return false;
  }
}

public int numKinds() {
  return 95;
}

public int symbolToKind(String symbol) {
  if (symbol.equals("Ref2RefT")) return 0;
  if (symbol.equals("ref2RefT")) return 1;
  if (symbol.equals("Ref2PrimT")) return 2;
  if (symbol.equals("ref2PrimT")) return 3;
  if (symbol.equals("Prim2RefT")) return 4;
  if (symbol.equals("prim2RefT")) return 5;
  if (symbol.equals("Prim2PrimT")) return 6;
  if (symbol.equals("prim2PrimT")) return 7;
  if (symbol.equals("Label2RefT")) return 8;
  if (symbol.equals("label2RefT")) return 9;
  if (symbol.equals("Label2PrimT")) return 10;
  if (symbol.equals("label2PrimT")) return 11;
  if (symbol.equals("SinkF2RefF")) return 12;
  if (symbol.equals("sinkF2RefF")) return 13;
  if (symbol.equals("SinkF2PrimF")) return 14;
  if (symbol.equals("sinkF2PrimF")) return 15;
  if (symbol.equals("Ref2RefF")) return 16;
  if (symbol.equals("ref2RefF")) return 17;
  if (symbol.equals("Ref2PrimF")) return 18;
  if (symbol.equals("ref2PrimF")) return 19;
  if (symbol.equals("Prim2RefF")) return 20;
  if (symbol.equals("prim2RefF")) return 21;
  if (symbol.equals("Prim2PrimF")) return 22;
  if (symbol.equals("prim2PrimF")) return 23;
  if (symbol.equals("ref2RefArgTStub")) return 24;
  if (symbol.equals("ref2RefRetTStub")) return 25;
  if (symbol.equals("prim2RefArgTStub")) return 26;
  if (symbol.equals("prim2RefRetTStub")) return 27;
  if (symbol.equals("ref2PrimTStub")) return 28;
  if (symbol.equals("prim2PrimTStub")) return 29;
  if (symbol.equals("PreFlowsTo")) return 30;
  if (symbol.equals("preFlowsTo")) return 31;
  if (symbol.equals("PostFlowsTo")) return 32;
  if (symbol.equals("postFlowsTo")) return 33;
  if (symbol.equals("MidFlowsTo")) return 34;
  if (symbol.equals("midFlowsTo")) return 35;
  if (symbol.equals("Transfer")) return 36;
  if (symbol.equals("transfer")) return 37;
  if (symbol.equals("transferSelf")) return 38;
  if (symbol.equals("%0")) return 39;
  if (symbol.equals("Pt")) return 40;
  if (symbol.equals("%1")) return 41;
  if (symbol.equals("Fpt")) return 42;
  if (symbol.equals("Store")) return 43;
  if (symbol.equals("%2")) return 44;
  if (symbol.equals("FptArr")) return 45;
  if (symbol.equals("StoreArr")) return 46;
  if (symbol.equals("%3")) return 47;
  if (symbol.equals("pt")) return 48;
  if (symbol.equals("fpt")) return 49;
  if (symbol.equals("fptArr")) return 50;
  if (symbol.equals("Obj2RefT")) return 51;
  if (symbol.equals("Obj2PrimT")) return 52;
  if (symbol.equals("Label2ObjT")) return 53;
  if (symbol.equals("SinkF2Obj")) return 54;
  if (symbol.equals("sink2Label")) return 55;
  if (symbol.equals("Label2Obj")) return 56;
  if (symbol.equals("%4")) return 57;
  if (symbol.equals("%5")) return 58;
  if (symbol.equals("%6")) return 59;
  if (symbol.equals("Label2Prim")) return 60;
  if (symbol.equals("%7")) return 61;
  if (symbol.equals("%8")) return 62;
  if (symbol.equals("SinkF2Prim")) return 63;
  if (symbol.equals("%9")) return 64;
  if (symbol.equals("%10")) return 65;
  if (symbol.equals("%11")) return 66;
  if (symbol.equals("Src2Sink")) return 67;
  if (symbol.equals("src2Label")) return 68;
  if (symbol.equals("%12")) return 69;
  if (symbol.equals("%13")) return 70;
  if (symbol.equals("Label2PrimFld")) return 71;
  if (symbol.equals("%14")) return 72;
  if (symbol.equals("Label2ObjX")) return 73;
  if (symbol.equals("%15")) return 74;
  if (symbol.equals("%16")) return 75;
  if (symbol.equals("Label2PrimFldArr")) return 76;
  if (symbol.equals("%17")) return 77;
  if (symbol.equals("assignPrimCtxt")) return 78;
  if (symbol.equals("assignPrimCCtxt")) return 79;
  if (symbol.equals("loadPrimCtxt")) return 80;
  if (symbol.equals("%18")) return 81;
  if (symbol.equals("loadPrimCtxtArr")) return 82;
  if (symbol.equals("%19")) return 83;
  if (symbol.equals("%20")) return 84;
  if (symbol.equals("Label2PrimFldStat")) return 85;
  if (symbol.equals("loadStatPrimCtxt")) return 86;
  if (symbol.equals("storePrimCtxt")) return 87;
  if (symbol.equals("%21")) return 88;
  if (symbol.equals("storePrimCtxtArr")) return 89;
  if (symbol.equals("%22")) return 90;
  if (symbol.equals("storeStatPrimCtxt")) return 91;
  if (symbol.equals("LabelRef3")) return 92;
  if (symbol.equals("LabelPrim3")) return 93;
  if (symbol.equals("Flow3")) return 94;
  throw new RuntimeException("Unknown symbol "+symbol);
}

public String kindToSymbol(int kind) {
  switch (kind) {
  case 0: return "Ref2RefT";
  case 1: return "ref2RefT";
  case 2: return "Ref2PrimT";
  case 3: return "ref2PrimT";
  case 4: return "Prim2RefT";
  case 5: return "prim2RefT";
  case 6: return "Prim2PrimT";
  case 7: return "prim2PrimT";
  case 8: return "Label2RefT";
  case 9: return "label2RefT";
  case 10: return "Label2PrimT";
  case 11: return "label2PrimT";
  case 12: return "SinkF2RefF";
  case 13: return "sinkF2RefF";
  case 14: return "SinkF2PrimF";
  case 15: return "sinkF2PrimF";
  case 16: return "Ref2RefF";
  case 17: return "ref2RefF";
  case 18: return "Ref2PrimF";
  case 19: return "ref2PrimF";
  case 20: return "Prim2RefF";
  case 21: return "prim2RefF";
  case 22: return "Prim2PrimF";
  case 23: return "prim2PrimF";
  case 24: return "ref2RefArgTStub";
  case 25: return "ref2RefRetTStub";
  case 26: return "prim2RefArgTStub";
  case 27: return "prim2RefRetTStub";
  case 28: return "ref2PrimTStub";
  case 29: return "prim2PrimTStub";
  case 30: return "PreFlowsTo";
  case 31: return "preFlowsTo";
  case 32: return "PostFlowsTo";
  case 33: return "postFlowsTo";
  case 34: return "MidFlowsTo";
  case 35: return "midFlowsTo";
  case 36: return "Transfer";
  case 37: return "transfer";
  case 38: return "transferSelf";
  case 39: return "%0";
  case 40: return "Pt";
  case 41: return "%1";
  case 42: return "Fpt";
  case 43: return "Store";
  case 44: return "%2";
  case 45: return "FptArr";
  case 46: return "StoreArr";
  case 47: return "%3";
  case 48: return "pt";
  case 49: return "fpt";
  case 50: return "fptArr";
  case 51: return "Obj2RefT";
  case 52: return "Obj2PrimT";
  case 53: return "Label2ObjT";
  case 54: return "SinkF2Obj";
  case 55: return "sink2Label";
  case 56: return "Label2Obj";
  case 57: return "%4";
  case 58: return "%5";
  case 59: return "%6";
  case 60: return "Label2Prim";
  case 61: return "%7";
  case 62: return "%8";
  case 63: return "SinkF2Prim";
  case 64: return "%9";
  case 65: return "%10";
  case 66: return "%11";
  case 67: return "Src2Sink";
  case 68: return "src2Label";
  case 69: return "%12";
  case 70: return "%13";
  case 71: return "Label2PrimFld";
  case 72: return "%14";
  case 73: return "Label2ObjX";
  case 74: return "%15";
  case 75: return "%16";
  case 76: return "Label2PrimFldArr";
  case 77: return "%17";
  case 78: return "assignPrimCtxt";
  case 79: return "assignPrimCCtxt";
  case 80: return "loadPrimCtxt";
  case 81: return "%18";
  case 82: return "loadPrimCtxtArr";
  case 83: return "%19";
  case 84: return "%20";
  case 85: return "Label2PrimFldStat";
  case 86: return "loadStatPrimCtxt";
  case 87: return "storePrimCtxt";
  case 88: return "%21";
  case 89: return "storePrimCtxtArr";
  case 90: return "%22";
  case 91: return "storeStatPrimCtxt";
  case 92: return "LabelRef3";
  case 93: return "LabelPrim3";
  case 94: return "Flow3";
  default: throw new RuntimeException("Unknown kind "+kind);
  }
}

public void process(Edge base) {
  switch (base.kind) {
  case 0: /* Ref2RefT */
    /* Ref2RefT + (_Pt *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(40)){
      addEdge(other.to, base.to, 51, base, other, false);
    }
    break;
  case 1: /* ref2RefT */
    /* ref2RefT => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 2: /* Ref2PrimT */
    /* Ref2PrimT + (_Pt *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(40)){
      addEdge(other.to, base.to, 52, base, other, false);
    }
    break;
  case 3: /* ref2PrimT */
    /* ref2PrimT => Ref2PrimT */
    addEdge(base.from, base.to, 2, base, false);
    break;
  case 4: /* Prim2RefT */
    /* Prim2RefT + (Label2Prim *) => %16 */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 75, base, other, false);
    }
    break;
  case 5: /* prim2RefT */
    /* prim2RefT => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 6: /* Prim2PrimT */
    /* Prim2PrimT + (Label2Prim *) => Label2Prim */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    break;
  case 7: /* prim2PrimT */
    /* prim2PrimT => Prim2PrimT */
    addEdge(base.from, base.to, 6, base, false);
    break;
  case 8: /* Label2RefT */
    /* Label2RefT + (* Pt) => Label2ObjT */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    break;
  case 9: /* label2RefT */
    /* label2RefT => Label2RefT */
    addEdge(base.from, base.to, 8, base, false);
    break;
  case 10: /* Label2PrimT */
    /* Label2PrimT => Label2Prim */
    addEdge(base.from, base.to, 60, base, false);
    break;
  case 11: /* label2PrimT */
    /* label2PrimT => Label2PrimT */
    addEdge(base.from, base.to, 10, base, false);
    break;
  case 12: /* SinkF2RefF */
    /* SinkF2RefF + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    break;
  case 13: /* sinkF2RefF */
    /* sinkF2RefF => SinkF2RefF */
    addEdge(base.from, base.to, 12, base, false);
    break;
  case 14: /* SinkF2PrimF */
    /* SinkF2PrimF => SinkF2Prim */
    addEdge(base.from, base.to, 63, base, false);
    break;
  case 15: /* sinkF2PrimF */
    /* sinkF2PrimF => SinkF2PrimF */
    addEdge(base.from, base.to, 14, base, false);
    break;
  case 16: /* Ref2RefF */
    /* _Ref2RefF + (%5 *) => %6 */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 59, base, other, false);
    }
    break;
  case 17: /* ref2RefF */
    /* ref2RefF => Ref2RefF */
    addEdge(base.from, base.to, 16, base, false);
    break;
  case 18: /* Ref2PrimF */
    /* _Ref2PrimF + (%7 *) => %8 */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 62, base, other, false);
    }
    break;
  case 19: /* ref2PrimF */
    /* ref2PrimF => Ref2PrimF */
    addEdge(base.from, base.to, 18, base, false);
    break;
  case 20: /* Prim2RefF */
    /* _Prim2RefF + (%10 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 21: /* prim2RefF */
    /* prim2RefF => Prim2RefF */
    addEdge(base.from, base.to, 20, base, false);
    break;
  case 22: /* Prim2PrimF */
    /* _Prim2PrimF + (%11 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(66)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 23: /* prim2PrimF */
    /* prim2PrimF => Prim2PrimF */
    addEdge(base.from, base.to, 22, base, false);
    break;
  case 24: /* ref2RefArgTStub */
    /* ref2RefArgTStub => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 25: /* ref2RefRetTStub */
    /* ref2RefRetTStub => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 26: /* prim2RefArgTStub */
    /* prim2RefArgTStub => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 27: /* prim2RefRetTStub */
    /* prim2RefRetTStub => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 28: /* ref2PrimTStub */
    /* ref2PrimTStub => Ref2PrimT */
    addEdge(base.from, base.to, 2, base, false);
    break;
  case 29: /* prim2PrimTStub */
    /* prim2PrimTStub => Prim2PrimT */
    addEdge(base.from, base.to, 6, base, false);
    break;
  case 30: /* PreFlowsTo */
    /* PreFlowsTo + (* Transfer) => %0 */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 39, base, other, false);
    }
    /* _PreFlowsTo + (%1 *) => Pt */
    for(Edge other : base.to.getInEdges(41)){
      addEdge(other.from, base.from, 40, base, other, false);
    }
    break;
  case 31: /* preFlowsTo */
    /* preFlowsTo => PreFlowsTo */
    addEdge(base.from, base.to, 30, base, false);
    break;
  case 32: /* PostFlowsTo */
    /* _PostFlowsTo + (* _Transfer) => %1 */
    for(Edge other : base.from.getInEdges(36)){
      addEdge(base.to, other.from, 41, base, other, false);
    }
    break;
  case 33: /* postFlowsTo */
    /* postFlowsTo => PostFlowsTo */
    addEdge(base.from, base.to, 32, base, false);
    break;
  case 34: /* MidFlowsTo */
    /* MidFlowsTo + (%0 *) => PreFlowsTo */
    for(Edge other : base.from.getInEdges(39)){
      addEdge(other.from, base.to, 30, base, other, false);
    }
    break;
  case 35: /* midFlowsTo */
    /* midFlowsTo => MidFlowsTo */
    addEdge(base.from, base.to, 34, base, false);
    break;
  case 36: /* Transfer */
    /* Transfer + (PreFlowsTo *) => %0 */
    for(Edge other : base.from.getInEdges(30)){
      addEdge(other.from, base.to, 39, base, other, false);
    }
    /* _Transfer + (_PostFlowsTo *) => %1 */
    for(Edge other : base.to.getOutEdges(32)){
      addEdge(other.to, base.from, 41, base, other, false);
    }
    break;
  case 37: /* transfer */
    /* transfer => Transfer */
    addEdge(base.from, base.to, 36, base, false);
    break;
  case 38: /* transferSelf */
    /* transferSelf => Transfer */
    addEdge(base.from, base.to, 36, base, false);
    break;
  case 39: /* %0 */
    /* %0 + (* MidFlowsTo) => PreFlowsTo */
    for(Edge other : base.to.getOutEdges(34)){
      addEdge(base.from, other.to, 30, base, other, false);
    }
    break;
  case 40: /* Pt */
    /* _Pt + (* Store[i]) => %2[i] */
    for(Edge other : base.from.getOutEdges(43)){
      addEdge(base.to, other.to, 44, base, other, true);
    }
    /* Pt + (%2[i] *) => Fpt[i] */
    for(Edge other : base.from.getInEdges(44)){
      addEdge(other.from, base.to, 42, base, other, true);
    }
    /* _Pt + (* StoreArr) => %3 */
    for(Edge other : base.from.getOutEdges(46)){
      addEdge(base.to, other.to, 47, base, other, false);
    }
    /* Pt + (%3 *) => FptArr */
    for(Edge other : base.from.getInEdges(47)){
      addEdge(other.from, base.to, 45, base, other, false);
    }
    /* _Pt + (* Ref2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(0)){
      addEdge(base.to, other.to, 51, base, other, false);
    }
    /* _Pt + (* Ref2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(2)){
      addEdge(base.to, other.to, 52, base, other, false);
    }
    /* Pt + (Label2RefT *) => Label2ObjT */
    for(Edge other : base.from.getInEdges(8)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* Pt + (SinkF2RefF *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(12)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* _Pt + (%4 *) => %5 */
    for(Edge other : base.to.getInEdges(57)){
      addEdge(other.from, base.from, 58, base, other, false);
    }
    /* Pt + (%6 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(59)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* Pt + (%8 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(62)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* _Pt + (%9 *) => %10 */
    for(Edge other : base.to.getInEdges(64)){
      addEdge(other.from, base.from, 65, base, other, false);
    }
    /* Pt + (%15 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(74)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* Pt + (%16 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(75)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* Pt + (%17 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(77)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* _Pt + (Label2ObjT *) => %18 */
    for(Edge other : base.to.getInEdges(53)){
      addEdge(other.from, base.from, 81, base, other, false);
    }
    /* _Pt + (Label2ObjX *) => %19 */
    for(Edge other : base.to.getInEdges(73)){
      addEdge(other.from, base.from, 83, base, other, false);
    }
    /* _Pt + (Label2PrimFld[i] *) => %20[i] */
    for(Edge other : base.to.getInEdges(71)){
      addEdge(other.from, base.from, 84, base, other, true);
    }
    /* Pt + (%21[i] *) => Label2PrimFld[i] */
    for(Edge other : base.from.getInEdges(88)){
      addEdge(other.from, base.to, 71, base, other, true);
    }
    /* Pt + (%22 *) => Label2PrimFldArr */
    for(Edge other : base.from.getInEdges(90)){
      addEdge(other.from, base.to, 76, base, other, false);
    }
    /* _Pt + (Label2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(56)){
      addEdge(other.from, base.from, 92, base, other, false);
    }
    break;
  case 41: /* %1 */
    /* %1 + (* _PreFlowsTo) => Pt */
    for(Edge other : base.to.getInEdges(30)){
      addEdge(base.from, other.from, 40, base, other, false);
    }
    break;
  case 42: /* Fpt */
    /* Fpt + (Label2ObjT *) => Label2ObjT */
    for(Edge other : base.from.getInEdges(53)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* Fpt + (SinkF2Obj *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(54)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    break;
  case 43: /* Store */
    /* Store[i] + (_Pt *) => %2[i] */
    for(Edge other : base.from.getOutEdges(40)){
      addEdge(other.to, base.to, 44, base, other, true);
    }
    break;
  case 44: /* %2 */
    /* %2[i] + (* Pt) => Fpt[i] */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 42, base, other, true);
    }
    break;
  case 45: /* FptArr */
    /* _FptArr + (* Obj2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(51)){
      addEdge(base.to, other.to, 51, base, other, false);
    }
    /* _FptArr + (* Obj2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(52)){
      addEdge(base.to, other.to, 52, base, other, false);
    }
    /* FptArr + (Label2ObjX *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(73)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    break;
  case 46: /* StoreArr */
    /* StoreArr + (_Pt *) => %3 */
    for(Edge other : base.from.getOutEdges(40)){
      addEdge(other.to, base.to, 47, base, other, false);
    }
    break;
  case 47: /* %3 */
    /* %3 + (* Pt) => FptArr */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 45, base, other, false);
    }
    break;
  case 48: /* pt */
    /* pt => Pt */
    addEdge(base.from, base.to, 40, base, false);
    break;
  case 49: /* fpt */
    /* fpt[i] => Fpt[i] */
    addEdge(base.from, base.to, 42, base, true);
    break;
  case 50: /* fptArr */
    /* fptArr => FptArr */
    addEdge(base.from, base.to, 45, base, false);
    break;
  case 51: /* Obj2RefT */
    /* Obj2RefT + (_FptArr *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(45)){
      addEdge(other.to, base.to, 51, base, other, false);
    }
    /* Obj2RefT + (Label2Obj *) => %15 */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 74, base, other, false);
    }
    /* Obj2RefT + (Label2PrimFldArr *) => %17 */
    for(Edge other : base.from.getInEdges(76)){
      addEdge(other.from, base.to, 77, base, other, false);
    }
    break;
  case 52: /* Obj2PrimT */
    /* Obj2PrimT + (_FptArr *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(45)){
      addEdge(other.to, base.to, 52, base, other, false);
    }
    /* Obj2PrimT + (Label2Obj *) => Label2Prim */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    /* Obj2PrimT + (Label2PrimFldArr *) => Label2Prim */
    for(Edge other : base.from.getInEdges(76)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    break;
  case 53: /* Label2ObjT */
    /* Label2ObjT + (* Fpt) => Label2ObjT */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    /* Label2ObjT => Label2Obj */
    addEdge(base.from, base.to, 56, base, false);
    /* Label2ObjT + (* _Pt) => %18 */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(base.from, other.from, 81, base, other, false);
    }
    break;
  case 54: /* SinkF2Obj */
    /* SinkF2Obj + (* Fpt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    /* _SinkF2Obj + (%12 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(69)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _SinkF2Obj + (%14 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(72)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    break;
  case 55: /* sink2Label */
    /* sink2Label + (* Label2Obj) => %4 */
    for(Edge other : base.to.getOutEdges(56)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    /* sink2Label + (* Label2Prim) => %7 */
    for(Edge other : base.to.getOutEdges(60)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    /* sink2Label + (* Label2Obj) => %9 */
    for(Edge other : base.to.getOutEdges(56)){
      addEdge(base.from, other.to, 64, base, other, false);
    }
    /* sink2Label + (* Label2Prim) => %11 */
    for(Edge other : base.to.getOutEdges(60)){
      addEdge(base.from, other.to, 66, base, other, false);
    }
    break;
  case 56: /* Label2Obj */
    /* Label2Obj + (sink2Label *) => %4 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    /* Label2Obj + (sink2Label *) => %9 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 64, base, other, false);
    }
    /* Label2Obj + (src2Label *) => %12 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    /* Label2Obj + (* Obj2RefT) => %15 */
    for(Edge other : base.to.getOutEdges(51)){
      addEdge(base.from, other.to, 74, base, other, false);
    }
    /* Label2Obj + (* Obj2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    /* Label2Obj + (* _Pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(base.from, other.from, 92, base, other, false);
    }
    break;
  case 57: /* %4 */
    /* %4 + (* _Pt) => %5 */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(base.from, other.from, 58, base, other, false);
    }
    break;
  case 58: /* %5 */
    /* %5 + (* _Ref2RefF) => %6 */
    for(Edge other : base.to.getInEdges(16)){
      addEdge(base.from, other.from, 59, base, other, false);
    }
    break;
  case 59: /* %6 */
    /* %6 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    break;
  case 60: /* Label2Prim */
    /* Label2Prim + (sink2Label *) => %7 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    /* Label2Prim + (sink2Label *) => %11 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 66, base, other, false);
    }
    /* Label2Prim + (src2Label *) => %13 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 70, base, other, false);
    }
    /* Label2Prim + (* Prim2RefT) => %16 */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 75, base, other, false);
    }
    /* Label2Prim + (* _assignPrimCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(78)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    /* Label2Prim + (* _assignPrimCCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(79)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    /* Label2Prim + (* Prim2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    /* Label2Prim + (* _storePrimCtxt[i]) => %21[i] */
    for(Edge other : base.to.getInEdges(87)){
      addEdge(base.from, other.from, 88, base, other, true);
    }
    /* Label2Prim + (* _storePrimCtxtArr) => %22 */
    for(Edge other : base.to.getInEdges(89)){
      addEdge(base.from, other.from, 90, base, other, false);
    }
    /* Label2Prim + (* _storeStatPrimCtxt[i]) => Label2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(91)){
      addEdge(base.from, other.from, 85, base, other, true);
    }
    /* Label2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 93, base, false);
    break;
  case 61: /* %7 */
    /* %7 + (* _Ref2PrimF) => %8 */
    for(Edge other : base.to.getInEdges(18)){
      addEdge(base.from, other.from, 62, base, other, false);
    }
    break;
  case 62: /* %8 */
    /* %8 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    break;
  case 63: /* SinkF2Prim */
    /* _SinkF2Prim + (%13 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(70)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    break;
  case 64: /* %9 */
    /* %9 + (* _Pt) => %10 */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(base.from, other.from, 65, base, other, false);
    }
    break;
  case 65: /* %10 */
    /* %10 + (* _Prim2RefF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(20)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    break;
  case 66: /* %11 */
    /* %11 + (* _Prim2PrimF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(22)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    break;
  case 67: /* Src2Sink */
    /* Src2Sink => Flow3 */
    addEdge(base.from, base.to, 94, base, false);
    break;
  case 68: /* src2Label */
    /* src2Label + (* Label2Obj) => %12 */
    for(Edge other : base.to.getOutEdges(56)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    /* src2Label + (* Label2Prim) => %13 */
    for(Edge other : base.to.getOutEdges(60)){
      addEdge(base.from, other.to, 70, base, other, false);
    }
    /* src2Label + (* Label2PrimFld) => %14 */
    for(Edge other : base.to.getOutEdges(71)){
      addEdge(base.from, other.to, 72, base, other, false);
    }
    break;
  case 69: /* %12 */
    /* %12 + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(54)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 70: /* %13 */
    /* %13 + (* _SinkF2Prim) => Src2Sink */
    for(Edge other : base.to.getInEdges(63)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 71: /* Label2PrimFld */
    /* Label2PrimFld + (src2Label *) => %14 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 72, base, other, false);
    }
    /* Label2PrimFld[i] + (* _Pt) => %20[i] */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(base.from, other.from, 84, base, other, true);
    }
    break;
  case 72: /* %14 */
    /* %14 + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(54)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 73: /* Label2ObjX */
    /* Label2ObjX => Label2Obj */
    addEdge(base.from, base.to, 56, base, false);
    /* Label2ObjX + (* FptArr) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(45)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    /* Label2ObjX + (* _Pt) => %19 */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(base.from, other.from, 83, base, other, false);
    }
    break;
  case 74: /* %15 */
    /* %15 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    break;
  case 75: /* %16 */
    /* %16 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    break;
  case 76: /* Label2PrimFldArr */
    /* Label2PrimFldArr + (* Obj2RefT) => %17 */
    for(Edge other : base.to.getOutEdges(51)){
      addEdge(base.from, other.to, 77, base, other, false);
    }
    /* Label2PrimFldArr + (* Obj2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    break;
  case 77: /* %17 */
    /* %17 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    break;
  case 78: /* assignPrimCtxt */
    /* _assignPrimCtxt + (Label2Prim *) => Label2Prim */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 79: /* assignPrimCCtxt */
    /* _assignPrimCCtxt + (Label2Prim *) => Label2Prim */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 80: /* loadPrimCtxt */
    /* _loadPrimCtxt + (%18 *) => Label2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%20[i] *) => Label2Prim */
    for(Edge other : base.to.getInEdges(84)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 81: /* %18 */
    /* %18 + (* _loadPrimCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 82: /* loadPrimCtxtArr */
    /* _loadPrimCtxtArr + (%19 *) => Label2Prim */
    for(Edge other : base.to.getInEdges(83)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 83: /* %19 */
    /* %19 + (* _loadPrimCtxtArr) => Label2Prim */
    for(Edge other : base.to.getInEdges(82)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 84: /* %20 */
    /* %20[i] + (* _loadPrimCtxt[i]) => Label2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 85: /* Label2PrimFldStat */
    /* Label2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Label2Prim */
    for(Edge other : base.to.getInEdges(86)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 86: /* loadStatPrimCtxt */
    /* _loadStatPrimCtxt[i] + (Label2PrimFldStat[i] *) => Label2Prim */
    for(Edge other : base.to.getInEdges(85)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 87: /* storePrimCtxt */
    /* _storePrimCtxt[i] + (Label2Prim *) => %21[i] */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 88, base, other, true);
    }
    break;
  case 88: /* %21 */
    /* %21[i] + (* Pt) => Label2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 71, base, other, true);
    }
    break;
  case 89: /* storePrimCtxtArr */
    /* _storePrimCtxtArr + (Label2Prim *) => %22 */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 90, base, other, false);
    }
    break;
  case 90: /* %22 */
    /* %22 + (* Pt) => Label2PrimFldArr */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 76, base, other, false);
    }
    break;
  case 91: /* storeStatPrimCtxt */
    /* _storeStatPrimCtxt[i] + (Label2Prim *) => Label2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 85, base, other, true);
    }
    break;
  }
}

public String[] outputRels() {
    String[] rels = {"LabelRef3", "LabelPrim3", "Flow3"};
    return rels;
}

public short kindToWeight(int kind) {
  switch (kind) {
  case 7:
    return (short)1;
  case 24:
    return (short)1;
  case 25:
    return (short)1;
  case 26:
    return (short)1;
  case 27:
    return (short)1;
  case 28:
    return (short)1;
  case 37:
    return (short)1;
  default:
    return (short)0;
  }
}

public boolean useReps() { return false; }

}