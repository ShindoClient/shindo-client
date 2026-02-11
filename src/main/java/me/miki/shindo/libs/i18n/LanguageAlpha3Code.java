package me.miki.shindo.libs.i18n;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public enum LanguageAlpha3Code {
    undefined("Undefined") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.undefined;
        }
    },
    aar("Afar") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.aa;
        }
    },
    aav("Austro-Asiatic languages"),
    abk("Abkhaz") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ab;
        }
    },
    ace("Achinese"),
    ach("Acoli"),
    ada("Adangme"),
    ady("Adyghe"),
    afa("Afro-Asiatic languages"),
    afh("Afrihili"),
    afr("Afrikaans") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.af;
        }
    },
    ain("Ainu (Japan)"),
    aka("Akan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ak;
        }
    },
    akk("Akkadian"),
    alb("Albanian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sq;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return sqi;
        }
    },
    ale("Aleut"),
    alg("Algonquian languages"),
    alt("Southern Altai"),
    alv("Atlantic-Congo languages"),
    amh("Amharic") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.am;
        }
    },
    ang("Old English"),
    anp("Angika"),
    apa("Apache languages"),
    aqa("Alacalufan languages"),
    aql("Algic languages"),
    ara("Arabic") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ar;
        }
    },
    arc("Official Aramaic"),
    arg("Aragonese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.an;
        }
    },
    arm("Armenian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.hy;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return hye;
        }
    },
    arn("Mapudungun"),
    arp("Arapaho"),
    art("Artificial languages"),
    arw("Arawak"),
    asm("Assamese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.as;
        }
    },
    ast("Asturian"),
    ath("Athapascan languages"),
    auf("Arauan languages"),
    aus("Australian languages"),
    ava("Avaric") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.av;
        }
    },
    ave("Avestan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ae;
        }
    },
    awa("Awadhi"),
    awd("Arawakan languages"),
    aym("Aymara") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ay;
        }
    },
    azc("Uto-Aztecan languages"),
    aze("Azerbaijani") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.az;
        }
    },
    bad("Banda languages"),
    bai("Bamileke languages"),
    bak("Bashkir") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ba;
        }
    },
    bal("Baluchi"),
    bam("Bambara") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bm;
        }
    },
    ban("Balinese"),
    baq("Basque") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.eu;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return eus;
        }
    },
    bas("Basa (Cameroon)"),
    bat("Baltic languages"),
    bej("Beja"),
    bel("Belarusian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.be;
        }
    },
    bem("Bemba (Zambia)"),
    ben("Bengali") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bn;
        }
    },
    ber("Berber languages"),
    bho("Bhojpuri"),
    bih("Bihari languages") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bh;
        }
    },
    bik("Bikol"),
    bin("Bini"),
    bis("Bislama") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bi;
        }
    },
    bla("Siksika"),
    bnt("Bantu languages"),
    bod("Tibetan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bo;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return tib;
        }
    },
    bos("Bosnian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bs;
        }
    },
    bra("Braj"),
    bre("Breton") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.br;
        }
    },
    btk("Batak languages"),
    bua("Buriat"),
    bug("Buginese"),
    bul("Bulgarian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bg;
        }
    },
    bur("Burmese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.my;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return mya;
        }
    },
    byn("Bilin"),
    cad("Caddo"),
    cai("Central American Indian languages"),
    car("Galibi Carib"),
    cat("Catalan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ca;
        }
    },
    cau("Caucasian languages"),
    cba("Chibchan languages"),
    ccn("North Caucasian languages"),
    ccs("South Caucasian languages"),
    cdc("Chadic languages"),
    cdd("Caddoan languages"),
    ceb("Cebuano"),
    cel("Celtic languages"),
    ces("Czech") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cs;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return cze;
        }
    },
    cha("Chamorro") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ch;
        }
    },
    chb("Chibcha"),
    che("Chechen") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ce;
        }
    },
    chg("Chagatai"),
    chi("Chinese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.zh;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return zho;
        }
    },
    chk("Chuukese"),
    chm("Mari (Russia)"),
    chn("Chinook jargon"),
    cho("Choctaw"),
    chp("Chipewyan"),
    chr("Cherokee"),
    chu("Church Slavic") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cu;
        }
    },
    chv("Chuvash") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cv;
        }
    },
    chy("Cheyenne"),
    cmc("Chamic languages"),
    cop("Coptic"),
    cor("Comish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kw;
        }
    },
    cos("Corsican") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.co;
        }
    },
    cpe("English based Creoles and pidgins"),
    cpf("French-Based Creoles and pidgins"),
    cpp("Portuguese-Based Creoles and pidgins"),
    cre("Cree") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cr;
        }
    },
    crh("Crimean Tatar"),
    crp("Creoles and pidgins"),
    csb("Kashubian"),
    csu("Central Sudanic languages"),
    cus("Cushitic languages"),
    cym("Welsh") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cy;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return wel;
        }
    },
    cze("Czech") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cs;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return ces;
        }
    },
    dak("Dakota"),
    dan("Danish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.da;
        }
    },
    dar("Dargwa"),
    day("Land Dayak languages"),
    del("Delaware"),
    den("Slave (Athapascan)"),
    deu("German") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.de;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return ger;
        }
    },
    dgr("Dogrib"),
    din("Dinka"),
    div("Dhivehi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.dv;
        }
    },
    dmn("Mande languages"),
    doi("Dogri"),
    dra("Dravidian languages"),
    dsb("Lower Sorbian"),
    dua("Duala"),
    dum("Middle Dutch"),
    dut("Dutch") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nl;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return nld;
        }
    },
    dyu("Dyula"),
    dzo("Dzongkha") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.dz;
        }
    },
    efi("Efik"),
    egx("Egyptian languages"),
    egy("Egyptian (Ancient)"),
    eka("Ekajuk"),
    ell("Modern Greek") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.el;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return gre;
        }
    },
    elx("Elamite"),
    eng("English") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.en;
        }
    },
    enm("Middle English"),
    epo("Esperanto") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.eo;
        }
    },
    est("Estonian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.et;
        }
    },
    esx("Eskimo-Aleut languages"),
    euq("Basque"),
    eus("Basque (family)") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.eu;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return baq;
        }
    },
    ewe("Ewe") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ee;
        }
    },
    ewo("Ewondo"),
    fan("Fang (Equatorial Guinea)"),
    fao("Faroese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fo;
        }
    },
    fas("Persian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fa;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return per;
        }
    },
    fat("Fanti"),
    fij("Fijian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fj;
        }
    },
    fil("Filipino"),
    fin("Finnish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fi;
        }
    },
    fiu("Finno-Ugrian languages"),
    fon("Fon"),
    fox("Formosan languages"),
    fra("French") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fr;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return fre;
        }
    },
    fre("French") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fr;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return fra;
        }
    },
    frm("Middle French"),
    fro("Old French"),
    frr("Northern Frisian"),
    frs("Eastern Frisian"),
    fry("West Frisian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fy;
        }
    },
    ful("Fula") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ff;
        }
    },
    fur("Friulian"),
    gaa("Ga"),
    gay("Gayo"),
    gba("Gbaya (Central African Republic)"),
    gem("Germanic languages"),
    geo("Georgian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ka;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return kat;
        }
    },
    ger("German") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.de;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return deu;
        }
    },
    gez("Geez"),
    gil("Gilbertese"),
    gla("Scottish Gaelic") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.gd;
        }
    },
    gle("Irish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ga;
        }
    },
    glg("Galician") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.gl;
        }
    },
    glv("Manx") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.gv;
        }
    },
    gme("East Germanic languages"),
    gmh("Middle High German"),
    gmq("North Germanic languages"),
    gmw("West Germanic languages"),
    goh("Old High German"),
    gon("Gondi"),
    gor("Gorontalo"),
    got("Gothic"),
    grb("Grebo"),
    grc("Ancient Greek"),
    gre("Modern Greek") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.el;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return ell;
        }
    },
    grk("Greek languages"),
    grn("Guaran\u00ED") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.gn;
        }
    },
    gsw("Swiss German"),
    guj("Gujarati") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.gu;
        }
    },
    gwi("Gwich\u02BCin"),
    hai("Haida"),
    hat("Haitian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ht;
        }
    },
    hau("Hausa") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ha;
        }
    },
    haw("Hawaiian"),
    heb("Hebrew") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.he;
        }
    },
    her("Herero") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.hz;
        }
    },
    hil("Hiligaynon"),
    him("Himachali languages"),
    hin("Hindi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.hi;
        }
    },
    hit("Hittite"),
    hmn("Hmong"),
    hmo("Hiri Motu") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ho;
        }
    },
    hmx("Hmong-Mien languages"),
    hok("Hokan languages"),
    hrv("Croatian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.hr;
        }
    },
    hsb("Upper Sorbian"),
    hun("Hungarian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.hu;
        }
    },
    hup("Hupa"),
    hye("Armenian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.hy;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return arm;
        }
    },
    hyx("Armenian (family)"),
    iba("Iban"),
    ibo("Igbo") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ig;
        }
    },
    ice("Icelandic") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.is;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return isl;
        }
    },
    ido("Ido") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.io;
        }
    },
    iii("Nuosu") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ii;
        }
    },
    iir("Indo-Iranian languages"),
    ijo("Ijo languages"),
    iku("Inuktitut") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.iu;
        }
    },
    ile("Interlingue") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ie;
        }
    },
    ilo("Iloko"),
    ina("Interlingua") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ia;
        }
    },
    inc("Interlingua (International Auxiliary Language Association)"),
    ind("Indonesian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.id;
        }
    },
    ine("Indo-European languages"),
    inh("Ingush"),
    ipk("Inupiaq") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ik;
        }
    },
    ira("Iranian languages"),
    iro("Iroquoian languages"),
    isl("Icelandic") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.is;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return ice;
        }
    },
    ita("Italian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.it;
        }
    },
    itc("Italic languages"),
    jav("Javanese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.jv;
        }
    },
    jbo("Lojban"),
    jpn("Japanese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ja;
        }
    },
    jpr("Judeo-Persian"),
    jpx("Japanese (family)"),
    jrb("Judeo-Arabic"),
    kaa("Kara-Kalpak"),
    kab("Kabyle"),
    kac("Kachin"),
    kal("Kalaallisut") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kl;
        }
    },
    kam("Kamba (Kenya)"),
    kan("Kannada") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kn;
        }
    },
    kar("Karen languages"),
    kas("Kashmiri") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ks;
        }
    },
    kat("Georgian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ka;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return geo;
        }
    },
    kau("Kanuri") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kr;
        }
    },
    kaw("Kawi"),
    kaz("Kazakh") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kk;
        }
    },
    kbd("Kabardian"),
    kdo("Kordofanian languages"),
    kha("Khasi"),
    khi("Khoisan languages"),
    khm("Central Khmer") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.km;
        }
    },
    kho("Khotanese"),
    kik("Kikuyu") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ki;
        }
    },
    kin("Kinyarwanda") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.rw;
        }
    },
    kir("Kirghiz") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ky;
        }
    },
    kmb("Kimbundu"),
    kok("Konkani"),
    kom("Komi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kv;
        }
    },
    kon("Kongo") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kg;
        }
    },
    kor("Korean") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ko;
        }
    },
    kos("Kosraean"),
    kpe("Kpelle"),
    krc("Karachay-Balkar"),
    krl("Karelian"),
    kro("Kru languages"),
    kru("Kurukh"),
    kua("Kuanyama") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.kj;
        }
    },
    kum("Kumyk"),
    kur("Kurdish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ku;
        }
    },
    kut("Kutenai"),
    lad("Ladino"),
    lah("Lahnda"),
    lam("Lamba"),
    lao("Lao") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.lo;
        }
    },
    lat("Latin") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.la;
        }
    },
    lav("Latvian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.lv;
        }
    },
    lez("Lezghian"),
    lim("Limburgan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.li;
        }
    },
    lin("Lingala") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ln;
        }
    },
    lit("Lithuanian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.lt;
        }
    },
    lol("Mongo"),
    loz("Lozi"),
    ltz("Luxembourgish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.lb;
        }
    },
    lua("Luba-Lulua"),
    lub("Luba-Katanga") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.lu;
        }
    },
    lug("Ganda") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.lg;
        }
    },
    lui("Luiseno"),
    lun("Lunda"),
    luo("Luo (Kenya and Tanzania)"),
    lus("Lushai"),
    mac("Macedonian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mk;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return mkd;
        }
    },
    mad("Madurese"),
    mag("Magahi"),
    mah("Marshallese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mh;
        }
    },
    mai("Maithili"),
    mak("Makasar"),
    mal("Malayalam") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ml;
        }
    },
    man("Mandingo"),
    mao("M\u0101ori") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mi;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return mri;
        }
    },
    map("Austronesian languages"),
    mar("Marathi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mr;
        }
    },
    mas("Masai"),
    may("Malay") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ms;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return msa;
        }
    },
    mdf("Moksha"),
    mdr("Mandar"),
    men("Mende (Sierra Leone)"),
    mga("Middle Irish"),
    mic("Mi'kmaq"),
    min("Minangkabau"),
    mis("Uncoded languages"),
    mkd("Macedonian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mk;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return mac;
        }
    },
    mkh("Mon-Khmer languages"),
    mlg("Malagasy") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mg;
        }
    },
    mlt("Maltese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mt;
        }
    },
    mnc("Manchu"),
    mni("Manipuri"),
    mno("Manobo languages"),
    moh("Mohawk"),
    mon("Mongolian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mn;
        }
    },
    mos("Mossi"),
    mri("M\u0101ori") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.mi;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return mao;
        }
    },
    msa("Malay") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ms;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return may;
        }
    },
    mul("Multiple languages"),
    mun("Munda languages"),
    mus("Creek"),
    mwl("Mirandese"),
    mwr("Marwari"),
    mya("Burmese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.my;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return bur;
        }
    },
    myn("Mayan languages"),
    myv("Erzya"),
    nah("Nahuatl languages"),
    nai("North American Indian"),
    nap("Neapolitan"),
    nau("Nauru") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.na;
        }
    },
    nav("Navajo") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nv;
        }
    },
    nbl("South Ndebele") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nr;
        }
    },
    nde("North Ndebele") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nd;
        }
    },
    ndo("Ndonga") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ng;
        }
    },
    nds("Low German"),
    nep("Nepali") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ne;
        }
    },
    New("Newari") {
        @Override
        public String toString() {
            return "new";
        }
    },
    ngf("Trans-New Guinea languages"),
    nia("Nias"),
    nic("Niger-Kordofanian languages"),
    niu("Niuean"),
    nld("Dutch") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nl;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return dut;
        }
    },
    nno("Norwegian Nynorsk") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nn;
        }
    },
    nob("Norwegian Bokm\u00E5l") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.nb;
        }
    },
    nog("Nogai"),
    non("Old Norse"),
    nor("Norwegian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.no;
        }
    },
    nqo("N'Ko"),
    nso("Pedi"),
    nub("Nubian languages"),
    nwc("Classical Newari"),
    nya("Nyanja") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ny;
        }
    },
    nym("Nyamwezi"),
    nyn("Nyankole"),
    nyo("Nyoro"),
    nzi("Nzima"),
    oci("Occitan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.oc;
        }
    },
    oji("Ojibwa") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.oj;
        }
    },
    omq("Oto-Manguean languages"),
    omv("Omotic languages"),
    ori("Oriya") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.or;
        }
    },
    orm("Oromo") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.om;
        }
    },
    osa("Osage"),
    oss("Ossetian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.os;
        }
    },
    ota("Ottoman Turkish"),
    oto("Otomian languages"),
    paa("Papuan languages"),
    pag("Pangasinan"),
    pal("Pahlavi"),
    pam("Pampanga"),
    pan("Panjabi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.pa;
        }
    },
    pap("Papiamento"),
    pau("Palauan"),
    peo("Old Persian"),
    per("Persian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.fa;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return fas;
        }
    },
    phi("Philippine languages"),
    phn("Phoenician"),
    plf("Central Malayo-Polynesian languages"),
    pli("P\u0101li") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.pi;
        }
    },
    pol("Polish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.pl;
        }
    },
    pon("Pohnpeian"),
    por("Portuguese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.pt;
        }
    },
    poz("Malayo-Polynesian languages"),
    pqe("Eastern Malayo-Polynesian languages"),
    pqw("Western Malayo-Polynesian languages"),
    pra("Prakrit languages"),
    pro("Old Proven\u00E7al"),
    pus("Pushto") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ps;
        }
    },
    que("Quechua") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.qu;
        }
    },
    qwe("Quechuan (family)"),
    raj("Rajasthani"),
    rap("Rapanui"),
    rar("Rarotongan"),
    roa("Romance languages"),
    roh("Romansh") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.rm;
        }
    },
    rom("Romany"),
    ron("Romanian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ro;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return rum;
        }
    },
    rum("Romansh") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ro;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return ron;
        }
    },
    run("Kirundi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.rn;
        }
    },
    rup("Macedo-Romanian"),
    rus("Russian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ru;
        }
    },
    sad("Sango"),
    sag("Sango") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sg;
        }
    },
    sah("Yakut"),
    sai("South American Indian languages"),
    sal("Salishan languages"),
    sam("Samaritan Aramaic"),
    san("Sanskrit") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sa;
        }
    },
    sas("Sasak"),
    sat("Santali"),
    scn("Sicilian"),
    sco("Scots"),
    sdv("Eastern Sudanic languages"),
    sel("Selkup"),
    sem("Semitic languages"),
    sga("Old Irish"),
    sgn("Sign languages"),
    shn("Shan"),
    sid("Sidamo"),
    sin("Sinhala") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.si;
        }
    },
    sio("Siouan languages"),
    sit("Sino-Tibetan languages"),
    sla("Slavic languages"),
    slk("Slovak") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sk;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return slo;
        }
    },
    slo("Slovak") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sk;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return slk;
        }
    },
    slv("Slovene") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sl;
        }
    },
    sma("Southern Sami"),
    sme("Northern Sami") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.se;
        }
    },
    smi("Sami languages"),
    smj("Lule Sami"),
    smn("Inari Sami"),
    smo("Samoan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sm;
        }
    },
    sms("Skolt Sami"),
    sna("Shona") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sn;
        }
    },
    snd("Sindhi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sd;
        }
    },
    snk("Soninke"),
    sog("Sogdian"),
    som("Somali") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.so;
        }
    },
    son("Songhai languages"),
    sot("Southern Sotho") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.st;
        }
    },
    spa("Spanish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.es;
        }
    },
    sqi("Albanian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sq;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return alb;
        }
    },
    sqj("Albanian languages"),
    srd("Sardinian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sc;
        }
    },
    srn("Sranan Tongo"),
    srp("Serbian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sr;
        }
    },
    srr("Serer"),
    ssa("Nilo-Saharan languages"),
    ssw("Swati") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ss;
        }
    },
    suk("Sukuma"),
    sun("Sundanese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.su;
        }
    },
    sus("Susu"),
    sux("Sumerian"),
    swa("Swahili") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sw;
        }
    },
    swe("Swedish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.sv;
        }
    },
    syc("Classical Syriac"),
    syd("Samoyedic languages"),
    syr("Syriac"),
    tah("Tahitian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ty;
        }
    },
    tai("Tai languages"),
    tam("Tamil") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ta;
        }
    },
    tat("Tatar") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tt;
        }
    },
    tbq("Tibeto-Burman languages"),
    tel("Telugu") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.te;
        }
    },
    tem("Timne"),
    ter("Tereno"),
    tet("Tetum"),
    tgk("Tajik") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tg;
        }
    },
    tgl("Tagalog") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tl;
        }
    },
    tha("Thai") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.th;
        }
    },
    tib("Tibetan") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.bo;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return bod;
        }
    },
    tig("Tigre"),
    tir("Tigrinya") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ti;
        }
    },
    tiv("Tiv"),
    tkl("Tokelau"),
    tlh("Klingon"),
    tli("Tlingit"),
    tmh("Tamashek"),
    tog("Tonga (Nyasa)"),
    ton("Tonga (Tonga Islands)") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.to;
        }
    },
    tpi("Tok Pisin"),
    trk("Turkic languages"),
    tsi("Tsimshian"),
    tsn("Tswana") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tn;
        }
    },
    tso("Tsonga") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ts;
        }
    },
    tuk("Turkmen") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tk;
        }
    },
    tum("Tumbuka"),
    tup("Tupi languages"),
    tur("Turkish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tr;
        }
    },
    tut("Altaic languages"),
    tuw("Tungus languages"),
    tvl("Tuvalu"),
    twi("Twi") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.tw;
        }
    },
    tyv("Tuvinian"),
    udm("Udmurt"),
    uga("Ugaritic"),
    uig("Uighur") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ug;
        }
    },
    ukr("Ukrainian") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.uk;
        }
    },
    umb("Umbundu"),
    und("Undetermined"),
    urd("Urdu") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ur;
        }
    },
    urj("Uralic languages"),
    uzb("Uzbek") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.uz;
        }
    },
    vai("Vai"),
    ven("Venda") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.ve;
        }
    },
    vie("Vietnamese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.vi;
        }
    },
    vol("Volap\u00FCk") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.vo;
        }
    },
    vot("Votic"),
    wak("Wakashan languages"),
    wal("Wolaytta"),
    war("Waray (Philippines)"),
    was("Washo"),
    wel("Welsh") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.cy;
        }


        @Override
        public Usage getUsage() {
            return Usage.BIBLIOGRAPHY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return cym;
        }
    },
    wen("Sorbian languages"),
    wln("Walloon") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.wa;
        }
    },
    wol("Wolof") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.wo;
        }
    },
    xal("Kalmyk"),
    xgn("Mongolian languages"),
    xho("Xhosa") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.xh;
        }
    },
    xnd("Na-Dene languages"),
    yao("Yao"),
    yap("Yapese"),
    yid("Yiddish") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.yi;
        }
    },
    yor("Yoruba") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.yo;
        }
    },
    ypk("Yupik languages"),
    zap("Zapotec"),
    zbl("Blissymbols"),
    zen("Zenaga"),
    zha("Zhuang") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.za;
        }
    },
    zho("Chinese") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.zh;
        }


        @Override
        public Usage getUsage() {
            return Usage.TERMINOLOGY;
        }


        @Override
        public LanguageAlpha3Code getSynonym() {
            return chi;
        }
    },
    zhx("Chinese (family)"),
    zle("East Slavic languages"),
    zls("South Slavic languages"),
    zlw("West Slavic languages"),
    znd("Zande languages"),
    zul("Zulu") {
        @Override
        public LanguageCode getAlpha2() {
            return LanguageCode.zu;
        }
    },
    zun("Zuni"),
    zxx("No linguistic content"),
    zza("Zaza");
    private final String name;


    LanguageAlpha3Code(String name) {
        this.name = name;
    }

    public static LanguageAlpha3Code getByCode(String code) {
        return getByCode(code, true);
    }

    public static LanguageAlpha3Code getByCodeIgnoreCase(String code) {
        return getByCode(code, false);
    }

    public static LanguageAlpha3Code getByCode(String code, boolean caseSensitive) {
        code = canonicalize(code, caseSensitive);

        if (code == null) {
            return null;
        }

        switch (code.length()) {
            case 2:
                break;

            case 3:
            case 9:
                return getByEnumName(code);

            default:
                return null;
        }

        code = LanguageCode.canonicalize(code, caseSensitive);
        LanguageCode alpha2 = LanguageCode.getByEnumName(code);

        if (alpha2 == null) {
            return null;
        }

        return alpha2.getAlpha3();
    }

    static LanguageAlpha3Code getByEnumName(String name) {
        try {
            return Enum.valueOf(LanguageAlpha3Code.class, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String canonicalize(String code, boolean caseSensitive) {
        if (code == null || code.length() == 0) {
            return null;
        }

        if (!caseSensitive) {
            code = code.toLowerCase();
        }

        if (code.equals("new")) {
            code = "New";
        }

        return code;
    }

    public static List<LanguageAlpha3Code> findByName(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex is null.");
        }


        Pattern pattern = Pattern.compile(regex);

        return findByName(pattern);
    }

    public static List<LanguageAlpha3Code> findByName(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }

        List<LanguageAlpha3Code> list = new ArrayList<LanguageAlpha3Code>();

        for (LanguageAlpha3Code entry : values()) {

            if (pattern.matcher(entry.getName()).matches()) {
                list.add(entry);
            }
        }

        return list;
    }

    public String getName() {
        return name;
    }

    public LanguageCode getAlpha2() {
        return null;
    }

    public Usage getUsage() {
        return Usage.COMMON;
    }

    public LanguageAlpha3Code getSynonym() {
        return this;
    }

    public LanguageAlpha3Code getAlpha3B() {
        if (getUsage() == Usage.BIBLIOGRAPHY) {
            return this;
        } else {
            return getSynonym();
        }
    }

    public LanguageAlpha3Code getAlpha3T() {
        if (getUsage() == Usage.TERMINOLOGY) {
            return this;
        } else {
            return getSynonym();
        }
    }

    public enum Usage {
        TERMINOLOGY,
        BIBLIOGRAPHY,
        COMMON
    }
}
