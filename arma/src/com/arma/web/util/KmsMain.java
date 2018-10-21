/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import com.arma.web.servlets.kms.KmsUtil;

public class KmsMain
{
    public static void main(String[] args)
    {
        kmsdate();
    }

    protected static void kmsdate()
    {
        KmsUtil.handleDate();
    }
}