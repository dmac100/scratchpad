package compiler.brushes;

import java.util.*;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.Brush;
import syntaxhighlighter.brush.RegExpRule;

public class BrushAsm extends Brush {
	public BrushAsm() {
		String directives = "TIMES ALIGN ALIGNB INCBIN EQU NOSPLIT SPLIT ABSOLUTE BITS SECTION SEGMENT ENDSECTION ENDSEGMENT"
			+ " ENDPROC EPILOGUE LOCALS PROC PROLOGUE USES ENDIF ELSE ELIF ELSIF IF DO ENDFOR ENDWHILE FOR REPEAT UNTIL WHILE EXIT"
			+ " ORG EXPORT IMPORT GROUP UPPERCASE SEG WRT LIBRARY .start .got .gotoff .gotpc .plt .sym";
		
		String storageKeywords = "DF EXTRN FWORD RESF TBYTE FAR NEAR SHORT BYTE WORD DWORD QWORD DQWORD HWORD"
			+ " DHWORD TWORD CDECL FASTCALL NONE PASCAL STDCALL DB DW DD DQ DDQ DT RESB RESW RESD RESQ RESDQ REST EXTERN GLOBAL COMMON";
		
		String asmKeywords = "AAA AAD AAM AAS AB ABGL AD ADC ADD ADD ADDPS ADDSS AMD AMD AND ANDNPS ANDPS"
			+ " ARPL BDWQ BOUND BSF BSR BSWAP BT BTR BTS CALL CBW CDQ CEOSZ CEPUZ CLC CLD CLI"
			+ " CLTS CMC CMOV CMOV CMP CMPS CMPSB CMPSD CMPSQ CMPSW CMPXCHG CMPXCHG"
			+ " CMPXCHG COMISS CPUID CQO CR CS CS CVTPI CVTPS CVTSI CVTSS CVTTPS CVTTSS CWD"
			+ " DAA DAS DEC DIV DIVPS DIVSS DN DQ DR EMMS ENTER EO EQ ET FABS FADD FBLD FBSTP"
			+ " FCHS FCLEX FCMOVN FCOM FCOMP FCOS FD FDECSTP FDISI FDIV FDIVR FEMMS FENI FFREE"
			+ " FIADD FICOM FIDIV FILD FIMUL FINCSTP FINIT FIST FISUB FLD FLDCW FLDENV FLDL FLDL"
			+ " FLDLG FLDLN FLDPI FLDZ FMUL FNCLEX FNDISI FNENI FNINIT FNOP FNSAVE FNSTCW FNSTENV"
			+ " FNSTSW FNSTSW FPATAN FPREM FPTAN FPU FRNDINT FRSTOR FSAVE FSCALE FSETPM FSIN FSINCOS"
			+ " FSQRT FST FSTCW FSTENV FSTSW FSUB FSUBR FTST FUCOM FUCOMP FXAM FXCH FXRSTOR FXSAVE"
			+ " FXTRACT FYL FYL HLT IBTS ICEBP ID IDIV IMUL IN INC INS INSB INSD INSW INT INVD INVLPG"
			+ " IP IRET IRETQ IRETW JCXZ JECXZ JE JA JAE JB JBE JC JE JG JGE JL JLE JNA JNAE JNB JNBE"
			+ " JNC JNE JNG JNGE JNL JNLE JNO JNP JNS JNZ JO JP JPE JPO JS JZ"
			+ " LAHF LAR LCS LDMXCSR LDS LEA LEAVE LES LFENCE LFS"
			+ " LGDT LGS LIDT LLDT LMSW LOADALL LOADALL LOADALL LOCK LODS LODSB LODSD LODSQ LODSW LOOP"
			+ " LOOPNE LOOPNZ LOOPZ LSL LSS LTR MASKMOVQ MAXPS MAXSS MFENCE MINPS MINSS"
			+ " MMX MOV MOVAPS MOVD MOVHLPS MOVHPS MOVLHPS MOVLPS MOVMSKPS MOVNTPS MOVNTQ"
			+ " MOVQ MOVS MOVSB MOVSD MOVSQ MOVSS MOVSW MOVSX MOVUPS MOVZX MUL MULPS MULSS NEG NOP NOT"
			+ " OR ORD ORPS OUT OUTS OUTSB OUTSB OUTSD OUTSW PACKSSDW PACKSSWB PACKUSWB PADDB PADDD PADDSB"
			+ " PADDSIW PADDSW PADDUSB PADDUSW PADDW PAND PAVEB PAVGB PAVGUSB PAVGW PCMP PCMPEQB PCMPEQD"
			+ " PCMPEQW PCMPGTB PCMPGTD PCMPGTW PDISTIB PEXTRW PF PFACC PFADD PFCMPEQ PFCMPGE PFCMPGT PFMAX"
			+ " PFMIN PFMUL PFRCP PFRCPIT PFRCPIT PFRSQIT PFRSQRT PFSUB PI PI PI PINSRW PMACHRIW PMADDWD PMAGW"
			+ " PMAXSW PMAXUB PMINSW PMINUB PMOVMSKB PMULHRIW PMULHRW PMULHRWA PMULHUW PMULHW PMULLW PMVGEZB PMVLZB"
			+ " PMVNZB PMVZB POP POP POPA POPAW POPF POPFQ POPFW POR PREFETCH PREFETCHNTA PREFETCHT PS PSADBW PSHUFW"
			+ " PSLLD PSLLQ PSLLW PSRAD PSRAW PSRLD PSRLQ PSRLW PSUBB PSUBD PSUBSB PSUBSIW PSUBSW PSUBUSB PSUBUSW"
			+ " PSUBW PUNPCKHBW PUNPCKHDQ PUNPCKHWD PUNPCKLBW PUNPCKLDQ PUNPCKLWD PUSH PUSHAW PUSHF PUSHFQ PUSHFW"
			+ " PXOR RCL RCPPS RCPSS RCR RDMSR RDPMC RDSHR RDTSC RET RETF ROL ROR RSDC RSLDT RSM RSQRTPS RSQRTSS"
			+ " SAHF SAL SALC SAR SBB SCASB SCASD SCASW SET SET SFENCE SFENCE SGDT SHL SHR SHUFPS SI SIDT SIMD"
			+ " SLDT SMI SMINT SMINTOLD SMSW SQRTPS SQRTSS SS SSE SSE STC STD STI STMXCSR STOS STOSB STOSD STOSQ"
			+ " STOSW STR SUB SUB SUBPS SUBSS SVDC SVLDT SVTS SYSCALL SYSENTER SYSEXIT SYSRET TEST TLB TR UCOMISS"
			+ " UD UMOV UN UNPCKHPS UNPCKLPS VERR VERW WAIT WBINVD WRMSR WRSHR XADD XBTS XCHG XLAT XLATB XM XOR XORPS XP";
		
		setRegExpRuleList(Arrays.asList(
			new RegExpRule(getKeywords(directives), Pattern.CASE_INSENSITIVE, "color2"),
			new RegExpRule("[a-z0-9_]+:", Pattern.CASE_INSENSITIVE, "color3"),
			new RegExpRule("\\b[A-D][HL]\\b", Pattern.CASE_INSENSITIVE, "color3"),
			new RegExpRule("\\b([A-D]X|[DS]I|[BS]P)\\b", Pattern.CASE_INSENSITIVE, "color3"),
			new RegExpRule("\\bE([A-D]X|[DS]I|[BS]P)\\b", Pattern.CASE_INSENSITIVE, "color3"),
			new RegExpRule("\\bR([A-D]X|[DS]I|[BS]P|[89]|1[0-5]|[89][WD]|1[0-5][WD])\\b", Pattern.CASE_INSENSITIVE, "color3"),
			new RegExpRule(";.*", "comments"),
			new RegExpRule(RegExpRule.doubleQuotedString, "string"),
			new RegExpRule("\\b([0-9]+h)\\b", Pattern.CASE_INSENSITIVE, "constants"),
			new RegExpRule("\\b(\\d+|0x[0-9a-f]+)\\b", Pattern.CASE_INSENSITIVE, "constants"),
			new RegExpRule("\\b\\d+(\\.\\d*)?([eE][+-]?\\d+)?\\b", Pattern.CASE_INSENSITIVE, "constants"),
			new RegExpRule(getKeywords(storageKeywords), Pattern.CASE_INSENSITIVE, "keyword"),
			new RegExpRule(getKeywords(asmKeywords),  Pattern.CASE_INSENSITIVE, "keyword")
		));
	}
}
