package me.miki.shindo.libs.i18n;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum LocaleCode {
    undefined(LanguageCode.undefined, CountryCode.UNDEFINED) {
        @Override
        public Locale toLocale() {
            return undefinedLocale;
        }
    },
    ar(LanguageCode.ar, null),
    ar_AE(LanguageCode.ar, CountryCode.AE),
    ar_BH(LanguageCode.ar, CountryCode.BH),
    ar_DZ(LanguageCode.ar, CountryCode.DZ),
    ar_EG(LanguageCode.ar, CountryCode.EG),
    ar_IQ(LanguageCode.ar, CountryCode.IQ),
    ar_JO(LanguageCode.ar, CountryCode.JO),
    ar_KW(LanguageCode.ar, CountryCode.KW),
    ar_LB(LanguageCode.ar, CountryCode.LB),
    ar_LY(LanguageCode.ar, CountryCode.LY),
    ar_MA(LanguageCode.ar, CountryCode.MA),
    ar_OM(LanguageCode.ar, CountryCode.OM),
    ar_QA(LanguageCode.ar, CountryCode.QA),
    ar_SA(LanguageCode.ar, CountryCode.SA),
    ar_SD(LanguageCode.ar, CountryCode.SD),
    ar_SY(LanguageCode.ar, CountryCode.SY),
    ar_TN(LanguageCode.ar, CountryCode.TN),
    ar_YE(LanguageCode.ar, CountryCode.YE),
    be(LanguageCode.be, null),
    be_BY(LanguageCode.be, CountryCode.BY),
    bg(LanguageCode.bg, null),
    bg_BG(LanguageCode.bg, CountryCode.BG),
    ca(LanguageCode.ca, null),
    ca_ES(LanguageCode.ca, CountryCode.ES),
    cs(LanguageCode.cs, null),
    cs_CZ(LanguageCode.cs, CountryCode.CZ),
    da(LanguageCode.da, null),
    da_DK(LanguageCode.da, CountryCode.DK),
    de(LanguageCode.de, null) {
        @Override
        public Locale toLocale() {
            return Locale.GERMAN;
        }
    },
    de_AT(LanguageCode.de, CountryCode.AT),
    de_CH(LanguageCode.de, CountryCode.CH),
    de_DE(LanguageCode.de, CountryCode.DE),
    de_LU(LanguageCode.de, CountryCode.LU),
    el(LanguageCode.el, null),
    el_CY(LanguageCode.el, CountryCode.CY),
    el_GR(LanguageCode.el, CountryCode.GR),
    en(LanguageCode.en, null) {
        @Override
        public Locale toLocale() {
            return Locale.ENGLISH;
        }
    },
    en_AU(LanguageCode.en, CountryCode.AU),
    en_CA(LanguageCode.en, CountryCode.CA),
    en_GB(LanguageCode.en, CountryCode.GB),
    en_HK(LanguageCode.en, CountryCode.HK),
    en_IE(LanguageCode.en, CountryCode.IE),
    en_IN(LanguageCode.en, CountryCode.IN),
    en_MT(LanguageCode.en, CountryCode.MT),
    en_NZ(LanguageCode.en, CountryCode.NZ),
    en_PH(LanguageCode.en, CountryCode.PH),
    en_SG(LanguageCode.en, CountryCode.SG),
    en_US(LanguageCode.en, CountryCode.US),
    en_ZA(LanguageCode.en, CountryCode.ZA),
    es(LanguageCode.es, null),
    es_AR(LanguageCode.es, CountryCode.AR),
    es_BO(LanguageCode.es, CountryCode.BO),
    es_CL(LanguageCode.es, CountryCode.CL),
    es_CO(LanguageCode.es, CountryCode.CO),
    es_CR(LanguageCode.es, CountryCode.CR),
    es_DO(LanguageCode.es, CountryCode.DO),
    es_EC(LanguageCode.es, CountryCode.EC),
    es_ES(LanguageCode.es, CountryCode.ES),
    es_GT(LanguageCode.es, CountryCode.GT),
    es_HN(LanguageCode.es, CountryCode.HN),
    es_MX(LanguageCode.es, CountryCode.MX),
    es_NI(LanguageCode.es, CountryCode.NI),
    es_PA(LanguageCode.es, CountryCode.PA),
    es_PE(LanguageCode.es, CountryCode.PE),
    es_PR(LanguageCode.es, CountryCode.PR),
    es_PY(LanguageCode.es, CountryCode.PY),
    es_SV(LanguageCode.es, CountryCode.SV),
    es_US(LanguageCode.es, CountryCode.US),
    es_UY(LanguageCode.es, CountryCode.UY),
    es_VE(LanguageCode.es, CountryCode.VE),
    et(LanguageCode.et, null),
    et_EE(LanguageCode.et, CountryCode.EE),
    fa(LanguageCode.fa, null),
    fa_IR(LanguageCode.fa, CountryCode.IR),
    fi(LanguageCode.fi, null),
    fi_FI(LanguageCode.fi, CountryCode.FI),
    fr(LanguageCode.fr, null) {
        @Override
        public Locale toLocale() {
            return Locale.FRENCH;
        }
    },
    fr_BE(LanguageCode.fr, CountryCode.BE),
    fr_CA(LanguageCode.fr, CountryCode.CA) {
        @Override
        public Locale toLocale() {
            return Locale.CANADA_FRENCH;
        }
    },
    fr_CH(LanguageCode.fr, CountryCode.CH),
    fr_FR(LanguageCode.fr, CountryCode.FR),
    fr_LU(LanguageCode.fr, CountryCode.LU),
    ga(LanguageCode.ga, null),
    ga_IE(LanguageCode.ga, CountryCode.IE),
    he(LanguageCode.he, null),
    he_IL(LanguageCode.he, CountryCode.IL),
    hi_IN(LanguageCode.hi, CountryCode.IN),
    hr(LanguageCode.hr, null),
    hr_HR(LanguageCode.hr, CountryCode.HR),
    hu(LanguageCode.hu, null),
    hu_HU(LanguageCode.hu, CountryCode.HU),
    id(LanguageCode.id, null),
    id_ID(LanguageCode.id, CountryCode.ID),
    is(LanguageCode.is, null),
    is_IS(LanguageCode.is, CountryCode.IS),
    it(LanguageCode.it, null) {
        @Override
        public Locale toLocale() {
            return Locale.ITALIAN;
        }
    },
    it_CH(LanguageCode.it, CountryCode.CH),
    it_IT(LanguageCode.it, CountryCode.IT),
    ja(LanguageCode.ja, null) {
        @Override
        public Locale toLocale() {
            return Locale.JAPANESE;
        }
    },
    ja_JP(LanguageCode.ja, CountryCode.JP),
    kk_KZ(LanguageCode.kk, CountryCode.KZ),
    ko(LanguageCode.ko, null) {
        @Override
        public Locale toLocale() {
            return Locale.KOREAN;
        }
    },
    ko_KR(LanguageCode.ko, CountryCode.KR),
    lt(LanguageCode.lt, null),
    lt_LT(LanguageCode.lt, CountryCode.LT),
    lv(LanguageCode.lv, null),
    lv_LV(LanguageCode.lv, CountryCode.LV),
    mk(LanguageCode.mk, null),
    mk_MK(LanguageCode.mk, CountryCode.MK),
    ms(LanguageCode.ms, null),
    ms_MY(LanguageCode.ms, CountryCode.MY),
    mt(LanguageCode.mt, null),
    mt_MT(LanguageCode.mt, CountryCode.MT),
    nb(LanguageCode.nb, null),
    nb_NO(LanguageCode.nb, CountryCode.NO),
    nl(LanguageCode.nl, null),
    nl_BE(LanguageCode.nl, CountryCode.BE),
    nl_NL(LanguageCode.nl, CountryCode.NL),
    nn_NO(LanguageCode.nn, CountryCode.NO),
    no(LanguageCode.no, null),
    no_NO(LanguageCode.no, CountryCode.NO),
    pl(LanguageCode.pl, null),
    pl_PL(LanguageCode.pl, CountryCode.PL),
    pt(LanguageCode.pt, null),
    pt_BR(LanguageCode.pt, CountryCode.BR),
    pt_PT(LanguageCode.pt, CountryCode.PT),
    ro(LanguageCode.ro, null),
    ro_MD(LanguageCode.ro, CountryCode.MD),
    ro_RO(LanguageCode.ro, CountryCode.RO),
    ru(LanguageCode.ru, null),
    ru_KZ(LanguageCode.ru, CountryCode.KZ),
    ru_RU(LanguageCode.ru, CountryCode.RU),
    se(LanguageCode.se, null),
    se_NO(LanguageCode.se, CountryCode.NO),
    sk(LanguageCode.sk, null),
    sk_SK(LanguageCode.sk, CountryCode.SK),
    sl(LanguageCode.sl, null),
    sl_SI(LanguageCode.sl, CountryCode.SI),
    sq(LanguageCode.sq, null),
    sq_AL(LanguageCode.sq, CountryCode.AL),
    sr(LanguageCode.sr, null),
    sr_BA(LanguageCode.sr, CountryCode.BA),
    sr_CS(LanguageCode.sr, CountryCode.CS),
    sr_ME(LanguageCode.sr, CountryCode.ME),
    sr_RS(LanguageCode.sr, CountryCode.RS),
    sv(LanguageCode.sv, null),
    sv_SE(LanguageCode.sv, CountryCode.SE),
    th(LanguageCode.th, null),
    th_TH(LanguageCode.th, CountryCode.TH),
    tr(LanguageCode.tr, null),
    tr_TR(LanguageCode.tr, CountryCode.TR),
    uk(LanguageCode.uk, null),
    uk_UA(LanguageCode.uk, CountryCode.UA),
    vi(LanguageCode.vi, null),
    vi_VN(LanguageCode.vi, CountryCode.VN),
    zh(LanguageCode.zh, null) {
        @Override
        public Locale toLocale() {
            return Locale.CHINESE;
        }
    },
    zh_CN(LanguageCode.zh, CountryCode.CN) {
        @Override
        public Locale toLocale() {
            return Locale.SIMPLIFIED_CHINESE;
        }
    },
    zh_HK(LanguageCode.zh, CountryCode.HK),
    zh_SG(LanguageCode.zh, CountryCode.SG),
    zh_TW(LanguageCode.zh, CountryCode.TW) {
        @Override
        public Locale toLocale() {
            return Locale.TRADITIONAL_CHINESE;
        }
    };


    private static final Locale undefinedLocale = getUndefinedLocale();

    private final LanguageCode language;
    private final CountryCode country;
    private final String string;


    LocaleCode(LanguageCode language, CountryCode country) {
        this.language = language;
        this.country = country;

        if (country == null) {
            string = language.name();
        } else {
            string = language.name() + "-" + country.name();
        }
    }

    public static LocaleCode getByCode(String code) {
        return getByCode(code, true);
    }

    public static LocaleCode getByCodeIgnoreCase(String code) {
        return getByCode(code, false);
    }

    public static LocaleCode getByCode(String code, boolean caseSensitive) {
        if (code == null) {
            return null;
        }

        switch (code.length()) {
            case 2:
            case 9:

                return getByCode(code, null, caseSensitive);

            case 5:
                return getByCombinedCode(code, caseSensitive, 2);

            case 19:
                return getByCombinedCode(code, caseSensitive, 9);

            default:
                return null;
        }
    }

    public static LocaleCode getByCode(String language, String country) {
        return getByCode(language, country, true);
    }

    public static LocaleCode getByCodeIgnoreCase(String language, String country) {
        return getByCode(language, country, false);
    }

    public static LocaleCode getByCode(String language, String country, boolean caseSensitive) {

        language = LanguageCode.canonicalize(language, caseSensitive);

        if (language == null) {

            return null;
        }

        country = CountryCode.canonicalize(country, caseSensitive);

        if (language.equals("undefined") && (country == null || country.equals("UNDEFINED"))) {
            return LocaleCode.undefined;
        }

        if (country == null) {
            return getByEnumName(language);
        } else {
            return getByEnumName(language + "_" + country);
        }
    }

    public static LocaleCode getByLocale(Locale locale) {
        if (locale == null) {
            return null;
        }


        String language = locale.getLanguage();


        String country = locale.getCountry();

        if ((language == null || language.length() == 0) &&
                (country == null || country.length() == 0)) {
            return LocaleCode.undefined;
        }


        return getByCode(language, country, true);
    }

    private static LocaleCode getByCombinedCode(String code, boolean caseSensitive, int splitPosition) {

        char separator = code.charAt(splitPosition);

        if (separator == '_') {
            if (caseSensitive && splitPosition == 2) {

                return getByEnumName(code);
            }
        } else if (separator != '-') {

            return null;
        }

        String language = code.substring(0, splitPosition);
        String country = code.substring(splitPosition + 1);

        return getByCode(language, country, caseSensitive);
    }

    private static LocaleCode getByEnumName(String name) {
        try {
            return Enum.valueOf(LocaleCode.class, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<LocaleCode> getByLanguage(String language) {
        return getByLanguage(language, true);
    }

    public static List<LocaleCode> getByLanguageIgnoreCase(String language) {
        return getByLanguage(language, false);
    }

    public static List<LocaleCode> getByLanguage(String language, boolean caseSensitive) {
        return getByLanguage(LanguageCode.getByCode(language, caseSensitive));
    }

    public static List<LocaleCode> getByLanguage(LanguageCode language) {
        List<LocaleCode> list = new ArrayList<LocaleCode>();

        if (language == null) {
            return list;
        }

        for (LocaleCode code : values()) {
            if (code.getLanguage() == language) {
                list.add(code);
            }
        }

        return list;
    }

    public static List<LocaleCode> getByCountry(String country) {
        return getByCountry(country, true);
    }

    public static List<LocaleCode> getByCountryIgnoreCase(String country) {
        return getByCountry(country, false);
    }

    public static List<LocaleCode> getByCountry(String country, boolean caseSensitive) {
        return getByCountry(CountryCode.getByCode(country, caseSensitive));
    }

    public static List<LocaleCode> getByCountry(CountryCode country) {
        List<LocaleCode> list = new ArrayList<LocaleCode>();

        if (country == null) {
            return list;
        }

        for (LocaleCode code : values()) {
            if (code.getCountry() == country) {
                list.add(code);
            }
        }

        return list;
    }

    private static Locale getUndefinedLocale() {
        try {

            Field root = Locale.class.getDeclaredField("ROOT");

            return (Locale) root.get(null);
        } catch (Exception e) {

            return new Locale("", "");
        }
    }

    public LanguageCode getLanguage() {
        return language;
    }

    public CountryCode getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return string;
    }

    public Locale toLocale() {
        if (country != null) {
            return new Locale(language.name(), country.name());
        } else {
            return new Locale(language.name());
        }
    }
}
