package me.miki.shindo.libs.i18n;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public enum LanguageCode {
    undefined() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.undefined;
        }
    },
    aa() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aar;
        }
    },
    ab() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.abk;
        }
    },
    ae() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ave;
        }
    },
    af() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.afr;
        }
    },
    ak() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aka;
        }
    },
    am() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.amh;
        }
    },
    an() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.arg;
        }
    },
    ar() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ara;
        }
    },
    as() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.asm;
        }
    },
    av() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ava;
        }
    },
    ay() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aym;
        }
    },
    az() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.aze;
        }
    },
    ba() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bak;
        }
    },
    be() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bel;
        }
    },
    bg() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bul;
        }
    },
    bh() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bih;
        }
    },
    bi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bis;
        }
    },
    bm() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bam;
        }
    },
    bn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ben;
        }
    },
    bo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bod;
        }
    },
    br() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bre;
        }
    },
    bs() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.bos;
        }
    },
    ca() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cat;
        }
    },
    ce() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.che;
        }
    },
    ch() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cha;
        }
    },
    co() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cos;
        }
    },
    cr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cre;
        }
    },
    cs() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ces;
        }
    },
    cu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.chu;
        }
    },
    cv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.chv;
        }
    },
    cy() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cym;
        }
    },
    da() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.dan;
        }
    },
    de() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.deu;
        }

        @Override
        public Locale toLocale() {
            return Locale.GERMAN;
        }
    },
    dv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.div;
        }
    },
    dz() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.dzo;
        }
    },
    ee() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ewe;
        }
    },
    el() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ell;
        }
    },
    en() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.eng;
        }


        @Override
        public Locale toLocale() {
            return Locale.ENGLISH;
        }
    },
    eo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.epo;
        }
    },
    es() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.spa;
        }
    },
    et() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.est;
        }
    },
    eu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.eus;
        }
    },
    fa() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fas;
        }
    },
    ff() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ful;
        }
    },
    fi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fin;
        }
    },
    fj() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fij;
        }
    },
    fo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fao;
        }
    },
    fr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fra;
        }

        @Override
        public Locale toLocale() {
            return Locale.FRENCH;
        }
    },
    fy() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.fry;
        }
    },
    ga() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.gle;
        }
    },
    gd() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.gla;
        }
    },
    gl() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.glg;
        }
    },
    gn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.grn;
        }
    },
    gu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.guj;
        }
    },
    gv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.glv;
        }
    },
    ha() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hau;
        }
    },
    he() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.heb;
        }
    },
    hi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hin;
        }
    },
    ho() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hmo;
        }
    },
    hr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hrv;
        }
    },
    ht() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hat;
        }
    },
    hu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hun;
        }
    },
    hy() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.hye;
        }
    },
    hz() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.her;
        }
    },
    ia() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ina;
        }
    },
    id() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ind;
        }
    },
    ie() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ile;
        }
    },
    ig() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ibo;
        }
    },
    ii() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.iii;
        }
    },
    ik() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ipk;
        }
    },
    io() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ido;
        }
    },
    is() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.isl;
        }
    },
    it() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ita;
        }

        @Override
        public Locale toLocale() {
            return Locale.ITALIAN;
        }
    },
    iu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.iku;
        }
    },
    ja() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.jpn;
        }

        @Override
        public Locale toLocale() {
            return Locale.JAPANESE;
        }
    },
    jv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.jav;
        }
    },
    ka() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kat;
        }
    },
    kg() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kon;
        }
    },
    ki() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kik;
        }
    },
    kj() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kua;
        }
    },
    kk() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kaz;
        }
    },
    kl() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kal;
        }
    },
    km() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.khm;
        }
    },
    kn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kan;
        }
    },
    ko() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kor;
        }

        @Override
        public Locale toLocale() {
            return Locale.KOREAN;
        }
    },
    kr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kau;
        }
    },
    ks() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kas;
        }
    },
    ku() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kur;
        }
    },
    kv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kom;
        }
    },
    kw() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.cor;
        }
    },
    ky() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kir;
        }
    },
    la() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lat;
        }
    },
    lb() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ltz;
        }
    },
    lg() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lug;
        }
    },
    li() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lim;
        }
    },
    ln() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lin;
        }
    },
    lo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lao;
        }
    },
    lt() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lit;
        }
    },
    lu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lub;
        }
    },
    lv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.lav;
        }
    },
    mg() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mlg;
        }
    },
    mh() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mah;
        }
    },
    mi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mri;
        }
    },
    mk() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mkd;
        }
    },
    ml() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mal;
        }
    },
    mn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mon;
        }
    },
    mr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mar;
        }
    },
    ms() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.msa;
        }
    },
    mt() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mlt;
        }
    },
    my() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.mya;
        }
    },
    na() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nau;
        }
    },
    nb() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nob;
        }
    },
    nd() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nde;
        }
    },
    ne() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nep;
        }
    },
    ng() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ndo;
        }
    },
    nl() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nld;
        }
    },
    nn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nno;
        }
    },
    no() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nor;
        }
    },
    nr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nbl;
        }
    },
    nv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nav;
        }
    },
    ny() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.nya;
        }
    },
    oc() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.oci;
        }
    },
    oj() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.oji;
        }
    },
    om() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.orm;
        }
    },
    or() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ori;
        }
    },
    os() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.oss;
        }
    },
    pa() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pan;
        }
    },
    pi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pli;
        }
    },
    pl() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pol;
        }
    },
    ps() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.pus;
        }
    },
    pt() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.por;
        }
    },
    qu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.que;
        }
    },
    rm() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.roh;
        }
    },
    rn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.run;
        }
    },
    ro() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ron;
        }
    },
    ru() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.rus;
        }
    },
    rw() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.kin;
        }
    },
    sa() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.san;
        }
    },
    sc() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.srd;
        }
    },
    sd() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.snd;
        }
    },
    se() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sme;
        }
    },
    sg() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sag;
        }
    },
    si() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sin;
        }
    },
    sk() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.slk;
        }
    },
    sl() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.slv;
        }
    },
    sm() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.smo;
        }
    },
    sn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sna;
        }
    },
    so() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.som;
        }
    },
    sq() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sqi;
        }
    },
    sr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.srp;
        }
    },
    ss() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ssw;
        }
    },
    st() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sot;
        }
    },
    su() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.sun;
        }
    },
    sv() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.swe;
        }
    },
    sw() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.swa;
        }
    },
    ta() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tam;
        }
    },
    te() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tel;
        }
    },
    tg() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tgk;
        }
    },
    th() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tha;
        }
    },
    ti() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tir;
        }
    },
    tk() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tuk;
        }
    },
    tl() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tgl;
        }
    },
    tn() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tsn;
        }
    },
    to() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ton;
        }
    },
    tr() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tur;
        }
    },
    ts() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tso;
        }
    },
    tt() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tat;
        }
    },
    tw() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.twi;
        }
    },
    ty() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.tah;
        }
    },
    ug() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.uig;
        }
    },
    uk() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ukr;
        }
    },
    ur() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.urd;
        }
    },
    uz() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.uzb;
        }
    },
    ve() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.ven;
        }
    },
    vi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.vie;
        }
    },
    vo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.vol;
        }
    },
    wa() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.wln;
        }
    },
    wo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.wol;
        }
    },
    xh() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.xho;
        }
    },
    yi() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.yid;
        }
    },
    yo() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.yor;
        }
    },
    za() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.zha;
        }
    },
    zh() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.zho;
        }


        @Override
        public Locale toLocale() {
            return Locale.CHINESE;
        }
    },
    zu() {
        @Override
        public LanguageAlpha3Code getAlpha3() {
            return LanguageAlpha3Code.zul;
        }
    };


    LanguageCode() {
    }

    public static LanguageCode getByCode(String code) {
        return getByCode(code, true);
    }

    public static LanguageCode getByCodeIgnoreCase(String code) {
        return getByCode(code, false);
    }

    public static LanguageCode getByCode(String code, boolean caseSensitive) {
        code = canonicalize(code, caseSensitive);

        if (code == null) {
            return null;
        }

        switch (code.length()) {
            case 2:
            case 9:
                return getByEnumName(code);

            case 3:
                break;

            default:
                return null;
        }

        LanguageAlpha3Code alpha3 = LanguageAlpha3Code.getByEnumName(code);

        if (alpha3 == null) {
            return null;
        }

        return alpha3.getAlpha2();
    }

    static LanguageCode getByEnumName(String name) {
        try {
            return Enum.valueOf(LanguageCode.class, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static LanguageCode getByLocale(Locale locale) {
        if (locale == null) {
            return null;
        }

        String language = locale.getLanguage();

        if (language == null || language.length() == 0) {
            return LanguageCode.undefined;
        }

        return getByCode(language, true);
    }

    static String canonicalize(String code, boolean caseSensitive) {
        if (code == null || code.length() == 0) {
            return null;
        }


        final String[] legacy = {"iw", "ji", "in"};
        final String[] official = {"he", "yi", "id"};

        for (int i = 0; i < legacy.length; ++i) {
            if (caseSensitive) {
                if (code.equals(legacy[i])) {
                    return official[i];
                }
            } else {
                if (code.equalsIgnoreCase(legacy[i])) {
                    return official[i];
                }
            }
        }

        if (caseSensitive) {
            return code;
        } else {
            return code.toLowerCase();
        }
    }

    public static List<LanguageCode> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }


        Pattern pattern = Pattern.compile(regex);

        return findByName(pattern);
    }

    public static List<LanguageCode> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }

        List<LanguageCode> list = new ArrayList<LanguageCode>();

        for (LanguageCode entry : values()) {

            if (pattern.matcher(entry.getName()).matches()) {
                list.add(entry);
            }
        }

        return list;
    }

    public String getName() {
        return getAlpha3().getName();
    }

    public Locale toLocale() {
        return new Locale(name());
    }

    public LanguageAlpha3Code getAlpha3() {
        return null;
    }
}
