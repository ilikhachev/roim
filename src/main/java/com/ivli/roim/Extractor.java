/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.image.Raster;
/**
 *
 * @author likhachev
 */
public interface Extractor {
    void apply(Raster aR) throws ArrayIndexOutOfBoundsException;
}
