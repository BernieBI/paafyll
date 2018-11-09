package no.hiof.matsl.pfyll.model;

public class Product {
    public int getId() {
        return id;
    }

    private int id;
    private String bildeUrl;
    private String alkohol;
    private String argang;
    private String biodynamisk;
    private String bitterhet;
    private String butikkategori;
    private String datotid;
    private String distributor;
    private String distrikt;
    private String emballasjetype;
    private String fairtrade;
    private String farge;
    private String friskhet;
    private String fylde;
    private String garvestoffer;
    private String gluten_lav_pa;
    private String grossist;
    private String korktype;
    private String kosher;
    private String lagringsgrad;
    private String land;
    private String literpris;
    private String lukt;
    private String metode;
    private String miljosmart_emballasje;
    private String okologisk;
    private String passertil01;
    private String passertil02;
    private String passertil03;
    private String pris;
    private String produktutvalg;
    private String produsent;
    private String rastoff;
    private String smak;
    private String sodme;
    private String sukker;
    private String syre;
    private String underdistrikt;
    private String varenavn;
    private String varenummer;
    private String varetype;
    private String vareurl;
    private String volum;
    private String hovedGTIN;
    public Product(){ }

    public Product(int id, String alkohol, String argang, String biodynamisk, String bitterhet, String butikkategori, String datotid, String distributor, String distrikt, String emballasjetype, String fairtrade, String farge, String friskhet, String fylde, String garvestoffer, String gluten_lav_pa, String grossist, String korktype, String kosher, String lagringsgrad, String land, String literpris, String lukt, String metode, String miljosmart_emballasje, String okologisk, String passertil01, String passertil02, String passertil03, String pris, String produktutvalg, String produsent, String rastoff, String smak, String sodme, String sukker, String syre, String underdistrikt, String varenavn, String varenummer, String varetype, String vareurl, String volum, String hovedGTIN) {
        this.id = id;
        this.alkohol = alkohol;
        this.argang = argang;
        this.biodynamisk = biodynamisk;
        this.bitterhet = bitterhet;
        this.butikkategori = butikkategori;
        this.datotid = datotid;
        this.distributor = distributor;
        this.distrikt = distrikt;
        this.emballasjetype = emballasjetype;
        this.fairtrade = fairtrade;
        this.farge = farge;
        this.friskhet = friskhet;
        this.fylde = fylde;
        this.garvestoffer = garvestoffer;
        this.gluten_lav_pa = gluten_lav_pa;
        this.grossist = grossist;
        this.korktype = korktype;
        this.kosher = kosher;
        this.lagringsgrad = lagringsgrad;
        this.land = land;
        this.literpris = literpris;
        this.lukt = lukt;
        this.metode = metode;
        this.miljosmart_emballasje = miljosmart_emballasje;
        this.okologisk = okologisk;
        this.passertil01 = passertil01;
        this.passertil02 = passertil02;
        this.passertil03 = passertil03;
        this.pris = pris;
        this.produktutvalg = produktutvalg;
        this.produsent = produsent;
        this.rastoff = rastoff;
        this.smak = smak;
        this.sodme = sodme;
        this.sukker = sukker;
        this.syre = syre;
        this.underdistrikt = underdistrikt;
        this.varenavn = varenavn;
        this.varenummer = varenummer;
        this.varetype = varetype;
        this.vareurl = vareurl;
        this.volum = volum;
        this.hovedGTIN = hovedGTIN;
    }

    public String getHovedGTIN() {
        return hovedGTIN;
    }

    public void setHovedGTIN(String hovedGTIN) {
        this.hovedGTIN = hovedGTIN;
    }


    public void setBildeUrl(String varenummer) {
        bildeUrl = "https://bilder.vinmonopolet.no/cache/300x300-0/"  + varenummer + "-1.jpg";
    }

    public String getBildeUrl() {
        return bildeUrl;
    }

    public String getAlkohol() {
        return alkohol;
    }

    public void setAlkohol(String alkohol) {
        this.alkohol = alkohol;
    }

    public String getArgang() {
        return argang;
    }

    public void setArgang(String argang) {
        this.argang = argang;
    }

    public String getBiodynamisk() {
        return biodynamisk;
    }

    public void setBiodynamisk(String biodynamisk) {
        this.biodynamisk = biodynamisk;
    }

    public String getBitterhet() {
        return bitterhet;
    }

    public void setBitterhet(String bitterhet) {
        this.bitterhet = bitterhet;
    }

    public String getButikkategori() {
        return butikkategori;
    }

    public void setButikkategori(String butikkategori) {
        this.butikkategori = butikkategori;
    }

    public String getDatotid() {
        return datotid;
    }

    public void setDatotid(String datotid) {
        this.datotid = datotid;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public String getDistrikt() {
        return distrikt;
    }

    public void setDistrikt(String distrikt) {
        this.distrikt = distrikt;
    }

    public String getEmballasjetype() {
        return emballasjetype;
    }

    public void setEmballasjetype(String emballasjetype) {
        this.emballasjetype = emballasjetype;
    }

    public String getFairtrade() {
        return fairtrade;
    }

    public void setFairtrade(String fairtrade) {
        this.fairtrade = fairtrade;
    }

    public String getFarge() {
        return farge;
    }

    public void setFarge(String farge) {
        this.farge = farge;
    }

    public String getFriskhet() {
        return friskhet;
    }

    public void setFriskhet(String friskhet) {
        this.friskhet = friskhet;
    }

    public String getFylde() {
        return fylde;
    }

    public void setFylde(String fylde) {
        this.fylde = fylde;
    }

    public String getGarvestoffer() {
        return garvestoffer;
    }

    public void setGarvestoffer(String garvestoffer) {
        this.garvestoffer = garvestoffer;
    }

    public String getGluten_lav_pa() {
        return gluten_lav_pa;
    }

    public void setGluten_lav_pa(String gluten_lav_pa) {
        this.gluten_lav_pa = gluten_lav_pa;
    }

    public String getGrossist() {
        return grossist;
    }

    public void setGrossist(String grossist) {
        this.grossist = grossist;
    }

    public String getKorktype() {
        return korktype;
    }

    public void setKorktype(String korktype) {
        this.korktype = korktype;
    }

    public String getKosher() {
        return kosher;
    }

    public void setKosher(String kosher) {
        this.kosher = kosher;
    }

    public String getLagringsgrad() {
        return lagringsgrad;
    }

    public void setLagringsgrad(String lagringsgrad) {
        this.lagringsgrad = lagringsgrad;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public String getLiterpris() {
        return literpris;
    }

    public void setLiterpris(String literpris) {
        this.literpris = literpris;
    }

    public String getLukt() {
        return lukt;
    }

    public void setLukt(String lukt) {
        this.lukt = lukt;
    }

    public String getMetode() {
        return metode;
    }

    public void setMetode(String metode) {
        this.metode = metode;
    }

    public String getMiljosmart_emballasje() {
        return miljosmart_emballasje;
    }

    public void setMiljosmart_emballasje(String miljosmart_emballasje) {
        this.miljosmart_emballasje = miljosmart_emballasje;
    }

    public String getOkologisk() {
        return okologisk;
    }

    public void setOkologisk(String okologisk) {
        this.okologisk = okologisk;
    }

    public String getPassertil01() {
        return passertil01;
    }

    public void setPassertil01(String passertil01) {
        this.passertil01 = passertil01;
    }

    public String getPassertil02() {
        return passertil02;
    }

    public void setPassertil02(String passertil02) {
        this.passertil02 = passertil02;
    }

    public String getPassertil03() {
        return passertil03;
    }

    public void setPassertil03(String passertil03) {
        this.passertil03 = passertil03;
    }

    public String getPris() {
        return pris;
    }

    public void setPris(String pris) {
        this.pris = pris;
    }

    public String getProduktutvalg() {
        return produktutvalg;
    }

    public void setProduktutvalg(String produktutvalg) {
        this.produktutvalg = produktutvalg;
    }

    public String getProdusent() {
        return produsent;
    }

    public void setProdusent(String produsent) {
        this.produsent = produsent;
    }

    public String getRastoff() {
        return rastoff;
    }

    public void setRastoff(String rastoff) {
        this.rastoff = rastoff;
    }

    public String getSmak() {
        return smak;
    }

    public void setSmak(String smak) {
        this.smak = smak;
    }

    public String getSodme() {
        return sodme;
    }

    public void setSodme(String sodme) {
        this.sodme = sodme;
    }

    public String getSukker() {
        return sukker;
    }

    public void setSukker(String sukker) {
        this.sukker = sukker;
    }

    public String getSyre() {
        return syre;
    }

    public void setSyre(String syre) {
        this.syre = syre;
    }

    public String getUnderdistrikt() {
        return underdistrikt;
    }

    public void setUnderdistrikt(String underdistrikt) {
        this.underdistrikt = underdistrikt;
    }

    public String getVarenavn() {
        return varenavn;
    }

    public void setVarenavn(String varenavn) {
        this.varenavn = varenavn;
    }

    public String getVarenummer() {
        return varenummer;
    }

    public void setVarenummer(String varenummer) {
        setBildeUrl(varenummer);
        this.varenummer = varenummer;
    }

    public String getVaretype() {
        return varetype;
    }

    public void setVaretype(String varetype) {
        this.varetype = varetype;
    }

    public String getVareurl() {
        return vareurl;
    }

    public void setVareurl(String vareurl) {
        this.vareurl = vareurl;
    }

    public String getVolum() {
        return volum;
    }

    public void setVolum(String volum) {
        this.volum = volum;
    }

}


