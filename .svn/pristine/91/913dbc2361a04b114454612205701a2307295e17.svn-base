/**
 * 2010.10.13 CheckObjListでcase 4に対するルーチン補強してみて正確に探せるように修正
 * 2010.06.30 static関数の場合、右に内部だけで関連が出てくるように修正.
 */
package changeminer.HandlerForRA;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TMeta;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;
import com.itplus.cm.parser.common.CMParserCommonData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * 090413:Cのsystem functionを除外するためにdoTDpdを修正
 * ファイル経路|関数名このような形態でgidを作ってfilterが動作する。
 *
 *
 */
public class HD_COM_C_NTT extends HandlerForRA {
    /**
     *
     */
    // ---------------------以下の部分はadminで追加すること.... ----------
    // OBJ
    // public final static int C_SOURCE_OBJECT = 511110;
    // ---------------------------------------------------------------------
    // OBJ
    // cではmainがある場合には、他のcでcall出来ないので。この部門を処理するため.

    private static final int C_FUNCTION_DECARE = 510003;
    private static final int C_FUNCTION_OBJECT = 510004;
    // DPD
    private static final int C_FUNCTION_CALL = 5100002; // c 関数 call
    private static final int SQL_CHECK = 5111102; // sprintf,strcpyにsqlがある場合は、正常なsql確認チェックをして、sqlの場合だけ保存する.

    private boolean mainflag;
    private String filestr;
    private String fileext;
    private Map<String, Integer> hfunname;
    private Map<String, String> hStaticfun;

    private static final long serialVersionUID = -3303948929891991953L;


    /*
     * 2015_03_13 GTONE ITO
     * C_SYSTEM_FUNCTION LIST
     */
    public String[] functionList =
            new String[] {"_chmod", "_clear87", "_close", "_control87", "_creat",
                    "_exit", "_fpreset", "_graphfreemem", "_graphgetmem", "_lrotr",
                    "_matherrl", "_open", "_OvrInitEms", "_OvrInitExt", "_read", "_rotl",
                    "_rotr", "_setcursortype", "_status87", "_strerror", "_tolower",
                    "_toupper", "_write", "ab", "abort", "abs", "absread", "abswrite",
                    "accept", "access", "acos", "alarm", "allocmem", "arc", "arg",
                    "asctime", "asin", "assert", "atan", "atan2", "atexit", "atof",
                    "atoi", "atol", "bar", "bar3d", "bdos", "bdosptr", "bind", "binmode",
                    "bioscom", "biosdisk", "biosequip", "bioskey", "biosmemory",
                    "biosprint", "biostime", "bless", "brk", "bsearch", "cabs", "caller",
                    "calloc", "ceil", "cgets", "chdir", "chmod", "chomp", "chop",
                    "chown", "chr", "chroot", "chsize", "circle", "cleardevice",
                    "clearerr", "clearviewport", "clock", "close", "closedir",
                    "closegraph", "clreol", "clrscr", "complex", "conj", "connect",
                    "coreleft", "cos", "cosh", "country", "cprintf", "cputs", "creat",
                    "creatnew", "creattemp", "crypt", "cscanf", "ctime", "ctrlbrk",
                    "ctrncmp", "dbmclose", "dbmopen", "defined", "delay", "delete",
                    "delline", "detectgraph", "die", "difftime", "disable", "div", "do",
                    "dosexterr", "dostounix", "drawpoly", "dump", "dup", "dup2", "each",
                    "ecvt", "ellipse", "enable", "endgrent", "endhostent", "endnetent",
                    "endprotoent", "endpwent", "endservent", "eof", "eval", "exec",
                    "exists", "exit", "exp", "fabs", "farcalloc", "farcoreleft",
                    "farfree", "farmalloc", "farrealloc", "fclose", "fcloseall", "fcntl",
                    "fcvt", "fdopen", "feof", "ferror", "fflush", "fgetc", "fgetchar",
                    "fgetpos", "fgets", "filelength", "fileno", "fillellipse",
                    "fillpoly", "findfirst", "findnext", "flock", "floodfill", "floor",
                    "flushall", "fmod", "fnmerge", "fnsplit", "fopen", "fork",
                    "formline", "FP_OFF", "FP_SEG", "fprintf", "fputc", "fputchar",
                    "fputs", "fread", "free", "freemem", "freopen", "frexp", "fscanf",
                    "fseek", "fsetpos", "fstat", "ftell", "ftime", "fwrite", "gcvt",
                    "geninterrupt", "getarccoords", "getaspectratio", "getbkcolor",
                    "getc", "getcbrk", "getch", "getchar", "getche", "getcolor",
                    "getcurdir", "getcwd", "getdate", "getdefaultpalette", "getdfree",
                    "getdisk", "getdrivername", "getdta", "getenv", "getfat", "getfatd",
                    "getfillpattern", "getfillsettings", "getftime", "getgraphmode",
                    "getgrent", "getgrgid", "getgrname", "gethostbyaddr",
                    "gethostbyname", "gethostent", "getimage", "getlinesettings",
                    "getlogin", "getmaxcolor", "getmaxmode", "getmaxx", "getmaxy",
                    "getmodename", "getmoderange", "getnetbyaddr", "getnetbyname",
                    "getnetent", "getpalette", "getpalettesize", "getpass",
                    "getpeername", "getpgrp", "getpid", "getpixel", "getppid",
                    "getpriority", "getprotobyname", "getprotobynumber", "getprotoent",
                    "getpsp", "getpwent", "getpwnam", "getpwuid", "gets",
                    "getservbyname", "getservbyport", "getservent", "getsockname",
                    "getsockopt", "gettext", "gettextinfo", "gettextsettings", "gettime",
                    "getvect", "getverify", "getviewsettings", "getw", "getx", "gety",
                    "glob", "gmtime", "gotoxy", "graphdefaults", "grapherrormsg",
                    "graphresult", "grep", "harderr", "hardresume", "hardretn",
                    "heapcheck", "heapcheckfree", "heapchecknode", "heapfillfree",
                    "heapwalk", "hex", "highvideo", "hypot", "imag", "imagesize",
                    "import", "index", "initgraph", "inport", "inportb", "insline",
                    "installuserdriver", "installuserfont", "int", "int86", "int86x",
                    "intdos", "intdosx", "intr", "ioctl", "isalnum", "isalpha",
                    "isascii", "isatty", "iscntrl", "isdigit", "isgraph", "islower",
                    "isprint", "ispunct", "isspace", "isupper", "isxdigit", "itoa",
                    "join", "kbhit", "keep", "keys", "kill", "labs", "lc", "lcfirst",
                    "ldexp", "ldiv", "length", "lfind", "line", "linerel", "lineto",
                    "link", "listen", "local", "localeconv", "localtime", "lock", "log",
                    "log10", "longjmp", "lowvideo", "lsearch", "lseek", "lstat", "ltoa",
                    "malloc", "map", "matherr", "max", "memccpy", "memchr", "memcmp",
                    "memcpy", "memicmp", "memmove", "memset", "min", "MK_FP", "mkdir",
                    "mktemp", "modf", "movedata", "moverel", "movetext", "moveto",
                    "movmem", "msgctl", "msgget", "msgrcv", "msgsnd", "my", "norm",
                    "normvideo", "nosound", "oct", "open", "opendir", "ord", "outport",
                    "outportb", "outtext", "outtextxy", "pack", "parsfnm", "peek",
                    "peekb", "perror", "pieslice", "pipe", "poke", "pokeb", "polar",
                    "poly", "pop", "pos", "pow", "pow10", "print", "printf", "push",
                    "putc", "putch", "putchar", "putenv", "putimage", "putpixel", "puts",
                    "puttext", "putw", "q", "qq", "qsort", "quotemeta", "qw", "qx",
                    "raise", "rand", "randbrd", "randbwr", "random", "randomize", "read",
                    "readdir", "readlink", "real", "realloc", "rectangle", "recv", "ref",
                    "registerbgidriver", "registerbgifont", "remove", "rename", "reset",
                    "restorecrtmode", "reverse", "rewind", "rewinddir", "rindex",
                    "rmdir", "sbrk", "scalar", "scanf", "searchpath", "sector", "seek",
                    "seekdir", "segread", "select", "select", "semctl", "semget",
                    "semop", "send", "setactivepage", "setallpalette", "setaspectratio",
                    "setbkcolor", "setblock", "setbuf", "setcbrk", "setcolor", "setdate",
                    "setdisk", "setdta", "setfillpattern", "setfillstyle", "setftime",
                    "setgraphbufsize", "setgraphmode", "setgrent", "sethostent",
                    "setjmp", "setlinestyle", "setlocale", "setmem", "setmode",
                    "setnetent", "setpalette", "setpgrp", "setpriority", "setprotoent",
                    "setpwent", "setrgbpalette", "setservent", "setsockopt",
                    "settextjustify", "settextstyle", "settime", "setusercharsize",
                    "setvbuf", "setvect", "setverify", "setviewport", "setvisualpage",
                    "setwritemode", "shift", "shmctl", "shmget", "shmread", "shmwrite",
                    "shutdown", "signal", "sin", "sinh", "sleep", "socket", "socketpair",
                    "sopen", "sort", "sound", "spawn", "splice", "split", "sprintf",
                    "sqrt", "srand", "sscanf", "stat", "stime", "stpcpy", "strcat",
                    "strchr", "strcmp", "strcmp", "strcmpi", "strcoll", "strcpy",
                    "strcspn", "strdup", "strerror", "stricmp", "strlen", "strlwr",
                    "strncat", "strncmp", "strncmpi", "strncpy", "strnicmp", "strpbrk",
                    "strrchr", "strrev", "strset", "strspn", "strstr", "strtod",
                    "strtok", "strtol", "strtoul", "strupr", "study", "substr", "swab",
                    "symlink", "syscall", "sysopen", "sysread", "system", "syswrite",
                    "tan", "tanh", "tell", "telldir", "textattr", "textbackground",
                    "textcolor", "textheight", "textmode", "textwidth", "tie", "tied",
                    "time", "times", "tmpfile", "tmpnam", "toascii", "tolower",
                    "toupper", "truncate", "tzset", "uc", "ucfirst", "ultoa", "umask",
                    "undef", "ungetc", "ungetch", "unixtodos", "unlink", "unlock",
                    "unpack", "unshift", "untie", "utime", "values", "vec", "wait",
                    "waitpid", "wantarray", "warn", "wherex", "wherey", "window", "write"};

    public HD_COM_C_NTT() {

    }

    /**
 *
 **/
    @Override
    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult)
            throws Exception {
        return RETURN_CONTINUE;
    }

    public void CheckObjList(CM_SRC cm_src, TObj tobj) {
        final TObj[] objlist = tobj.getTObjList();
        if (objlist == null) {
            return;
        }
        for (final TObj tmpobj : objlist) {
            CheckObjList(cm_src, tmpobj);
            String objname = tmpobj.getName();
            tmpobj.getName();
            final int idx = objname.indexOf('(');
            if (idx > 0) {
                objname = objname.substring(0, idx).trim();
            }
            if (tmpobj.getType() == C_FUNCTION_OBJECT) {
                tmpobj.setKeyValidation(100);// 追加をして、そのobj generateに搭載するようだ...
                                             // 2008.10.23日修正 if (objname.equals("main"))
                                             // {
                mainflag = true;
                log.debug("find main", tmpobj.getName());
            }
            /* --------------- SITEごとに変更が必要で-------------------------- */
            /*
             * CASE 1:ファイル名は大文字イク（??）関数は小文字としながら同じ名前を持つ場合mainがあることで判断される.
             * else if (objname.toUpperCase().equals(filestr.toUpperCase()) ) {
             * mainflag = true;
             * log.debug("find main", tmpobj.getName());
             * }
             */
            /* CASE 2:ファイル名と関数が、大小文字区分が同じで同じ名前、.cにはファイル名と関数名が同じものがあってもmainで判断されない場合 */
            /*
             * else if (objname.equals(filestr) && !fileext.equals("c")) {
             * mainflag = true;
             * log.debug("find main", tmpobj.getName());
             * }
             */
            /*
             * CASE 3:ファイル名と関数が大小文字区分が同じで同じ名前ならmainで判断される場合
             * else if (objname.equals(filestr) ) {
             * mainflag = true;
             * log.debug("find main", tmpobj.getName());
             * }
             */


            /* ---------------------------------------------------------- */
            else {


                /*
                 * CASE 4:\\銀行CASEとして関数名とファイル名が同じでパラメータ一部が同じならば
                 * stmserve(SUCOMMONAREA * CommonArea , SUINPUTAREA * InputArea ,
                 * SUOUTPUTAREA * OutputArea , FMT_CTRL_DATA_T * FmtCtlData , SUAPRVAREA *
                 * AprvArea)
                 *
                 * Pattern p = Pattern.compile("\\([\\s]*SUCOMMONAREA([\\s]+|\\*)");
                 * Matcher m = p.matcher(fullobjname);
                 * if ( m.find()) {
                 * mainflag = true;
                 * log.debug("find main", tmpobj.getName());
                 * }
                 * else
                 */

                if (!hfunname.containsKey(objname)) {
                    // log.debug("push hash", objname);
                    final Integer val = Integer.valueOf(0);
                    hfunname.put(objname, val);
                }
            }

            // staticで宣言された関数なのかチェック
            if (tmpobj.getType() == C_FUNCTION_DECARE
                    || tmpobj.getType() == C_FUNCTION_OBJECT) {
                final TMeta[] mt = tmpobj.getTMetaList();
                for (final TMeta element : mt) {
                    if (Objects.equals(element.getName(), "5100410")) {
                        if (element.getValue().startsWith("static ")) {
                            hStaticfun.put(objname, objname);
                        }
                    }
                }
            }
        }
    }

    public void CheckDPDList(TObj tobj) {
        final TObj[] objlist = tobj.getTObjList();
        if (objlist == null) {
            return;
        }
        for (final TObj tmpobj : objlist) {
            CheckDPDList(tmpobj);
            final TDpd[] dpdlist = tmpobj.getTDpdList();
            if (dpdlist == null) {
                continue;
            }
            for (final TDpd tmpdpd : dpdlist) {
                if (tmpdpd == null) {
                    continue;
                }

                if (tmpdpd.getType() == C_FUNCTION_CALL) {
                    String dpd_name = tmpdpd.getName();
                    if (dpd_name.indexOf('(') > 0) {
                        dpd_name = dpd_name.substring(0, dpd_name.indexOf('(')).trim();
                    }
                    if (hfunname.containsKey(dpd_name)) {
                        final Integer val = hfunname.get(dpd_name);
                        final int cnt = val.intValue() + 1;
                        hfunname.put(dpd_name, Integer.valueOf(cnt));
                        // log.debug("push hash", dpd_name + ", count =" + cnt);
                    }
                }
            }

        }

    }

    /**
 *
 **/
    @Override
    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        // log.debug("doTObj", "type=" + tobj.getType() + ",name=" + tobj.getName());
        if (depth == 0) {
            // check_ecc_file(cm_src);

            filestr = cm_src.getSNAME();
            fileext = "c"; // default .c
            final int idx = filestr.indexOf('.');
            if (idx > 0) {
                filestr = filestr.substring(0, idx);
                fileext =
                        cm_src.getSNAME().substring(idx + 1, cm_src.getSNAME().length());
            }
            log.debug("filestr ", filestr);
            log.debug("fileext ", fileext);

            mainflag = false;
            if (hfunname == null) {
                hfunname = new HashMap<String, Integer>();
            } else {
                hfunname.clear();
            }

            if (hStaticfun == null) {
                hStaticfun = new HashMap<String, String>();
            } else {
                hStaticfun.clear();
            }

            CheckObjList(cm_src, tobj);
            CheckDPDList(tobj);
        }
        // if (tobj.getType() == 511101) {
        // tobj.add( new TObj(C_SOURCE_OBJECT, filestr, filestr, 100, tobj.getTLocation())
        // );
        // }


        // log.debug("HANDLER", depth + " : " + tobj.getName() + " : " +
        // tobj.getTempMap());
        return RETURN_CONTINUE;
    }

    /**
 *
 **/
    @Override
    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq)
            throws SQLException {
        String dpd_name = tdpd.getName();

        if (tdpd.getType() == C_FUNCTION_CALL) {
            // log.debug("doTDpd(DPD)=================>>>>","gid="+tdpd.getGID()+",dpd=" +
            // tdpd.getName());



            if (tdpd.getGID() == null) {
                // tdpd.setGID("C:/CM50/SRC/src/20071221000022/src/mnmg1130_ss.pc|" +
                // dpd_name);

                // functionに対するfilter処理のためにはファイル経路+ "|" + function nameでgidを作らなければならない。
                // generateGIDでは再び必要な形態のgidに変更
                if (dpd_name.indexOf('(') > 0) {
                    dpd_name = dpd_name.substring(0, dpd_name.indexOf('(')).trim();
                    final String fileName =
                            Environment.getSourceDir() + "/" + cm_src.getCOLLECT_ID()
                                    + cm_src.getSPATH() + cm_src.getSNAME();
                    tdpd.setGID(fileName + "|" + dpd_name);
                    log.debug("new gid = ", fileName + "|" + dpd_name);

                    /*
                     * 2015_03_13 GTONE ITO
                     * システムFunctionがDPD登録される問題があるため、DPD生成時にC_SYSTEM_FUNCTIONはDPD生成を中断する
                     */
                    if (checkSystemFunction(dpd_name)) {
                        log.debug("found system function >> ", dpd_name);
                        return RETURN_CONTINUE;
                    }

                    tdpd.setKeyValidation(100);

                }
            }
            /*
             * if (dpd_name.indexOf('(') > 0) {
             * dpd_name = dpd_name.substring(0, dpd_name.indexOf('(') ).trim();
             * }
             * int cnt = 0;
             * if (hfunname.containsKey(dpd_name)) {
             * Integer val = (Integer)hfunname.get(dpd_name);
             * cnt = val.intValue();
             * }
             * if (mainflag || cnt > 0) {
             * String str = new String();
             * str = filestr + "." + dpd_name;
             * Integer val;
             * long gid = FileUtil.getGID("<EC>", str);
             * Long lgid = new Long(gid);
             * tdpd.setGID(lgid.toString());
             * log.debug("(inside)doTDpd("+lgid.toString()+")=================>>>>",str);
             * } else {
             * String str = new String();
             * str = dpd_name;
             * Integer val;
             * long gid = FileUtil.getGID("<EC>", str);
             * Long lgid = new Long(gid);
             * log.debug("(outside)doTDpd("+lgid.toString()+")=================>>>>",str);
             * tdpd.setGID(lgid.toString());
             * }
             */
        }
        // log.trace("HANDLER", depth + " : " + tdpd.getName() + " : " +
        // tdpd.getTempMap());
        return RETURN_CONTINUE;
    }

    /**
 *
 **/
    @Override
    public long generateGID(String prefix, TObj tobj) {
        String obj_name = tobj.getName();
        /*
         * if (tobj.getType() == C_SOURCE_OBJECT) {
         * String str = new String();
         * str = obj_name;
         * //log.debug("generateGID(OBJ)=================>>>>",str);
         * return FileUtil.getGID("<C>", str);
         * }
         */
        if (tobj.getType() == C_FUNCTION_OBJECT) {
            if (obj_name.indexOf('(') > 0) {
                obj_name = obj_name.substring(0, obj_name.indexOf('(')).trim();
            }
            obj_name = obj_name.trim();
            if (hfunname.containsKey(obj_name)) {
                final Integer val = hfunname.get(obj_name);
                val.intValue();
            }
            // 1個の関数だけある場合には,ライブラリーで見る...
            // && hfunname.size() > 1がルーチンを入れることができない。...
            // 一個だけなっている場合もある。..... ?.?

            if (mainflag) { // || cnt > 0) {
                final String str = filestr + "." + obj_name;
                // log.debug("(main)generateGID(OBJ)=================>>>>",str);
                tobj.setGID(str);
                return FileUtil.getGID("<EC>", str);

            }
            String str = "";
            if (hStaticfun.containsKey(obj_name)) {
                str = filestr + "." + obj_name;
            } else {
                str = obj_name;
            }

            // log.debug("generateGID(OBJ)=================>>>>",str);
            tobj.setGID(str);
            return FileUtil.getGID("<EC>", str);
        }
        return 0L;
    }

    /**
 *
 **/
    @Override
    public long generateGID(String prefix, TDpd tdpd) {
        String dpd_name = tdpd.getName();
        if (tdpd.getType() == C_FUNCTION_CALL) {
            if (dpd_name.indexOf('(') > 0) {
                dpd_name = dpd_name.substring(0, dpd_name.indexOf('(')).trim();
            }
            dpd_name = dpd_name.trim();
            int cnt = 0;
            if (hfunname.containsKey(dpd_name)) {
                final Integer val = hfunname.get(dpd_name);
                cnt = val.intValue();
            }

            if (mainflag) { // 開始プログラム
                if (cnt == 0) { // 内部に関数が無ければ外部で...
                    final String str = dpd_name;
                    tdpd.setGID(str);
                    return FileUtil.getGID("<EC>", str);
                }
                final String str = filestr + "." + dpd_name;
                // log.debug("(inside)generateGID(DPD)=================>>>>",str);
                tdpd.setGID(str);
                return FileUtil.getGID("<EC>", str);
            }
            String str = "";
            if (hStaticfun.containsKey(dpd_name)) {
                str = filestr + "." + dpd_name;
            } else {
                str = dpd_name;
            }
            tdpd.setGID(str);
            return FileUtil.getGID("<EC>", str);
        }
        return 0L;
    }


    /*
     * 2015_03_13 GTONE ITO
     * SYSTEM_FUNCTION チェック
     */
    public boolean checkSystemFunction(String methodName) {

        boolean isSystemFunc = false;
        System.out.println("******************************>>>>>>>>>>>>>> " + methodName);
        for (final String funcName : functionList) {

            if (methodName.startsWith(funcName)) {
                isSystemFunc = true;
                break;
            }

        }
        return isSystemFunc;
    }


}
