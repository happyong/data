/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.fund;

import java.util.Comparator;

public class FundComparator implements Comparator<String>
{
    private int sort_type;
    private boolean asc;

    public FundComparator(boolean asc, int sort_type)
    {
        this.asc = asc;
        this.sort_type = sort_type;
    }

    public int compare(String code_b1, String code_b2)
    {
        return FundCacher.compare(asc, sort_type, code_b1, code_b2);
    }
}
