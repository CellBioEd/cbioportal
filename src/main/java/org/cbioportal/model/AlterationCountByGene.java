package org.cbioportal.model;

import java.math.BigDecimal;

public class AlterationCountByGene extends AlterationCountBase {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private Integer numberOfAlteredCases;
    private BigDecimal qValue;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public Integer getNumberOfAlteredCases() {
        return numberOfAlteredCases;
    }

    public void setNumberOfAlteredCases(Integer numberOfAlteredCases) {
        this.numberOfAlteredCases = numberOfAlteredCases;
    }

    public BigDecimal getQValue() {
        return qValue;
    }

    public void setQValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

    @Override
    public String getUniqueEventKey() {
        return hugoGeneSymbol;
    }

    @Override
    public String[] getHugoGeneSymbols() {
        return new String[]{hugoGeneSymbol};
    }

    @Override
    public Integer[] getEntrezGeneIds() {
        return new Integer[]{entrezGeneId};
    }

}
