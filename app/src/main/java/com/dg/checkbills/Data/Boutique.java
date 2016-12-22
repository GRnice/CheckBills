package com.dg.checkbills.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Remy on 31/10/2016.
 */
public class Boutique extends Data
{
    /**
     * Boutique a un nom et un ID
     */

    private  static  final  long serialVersionUID =  1332792844476720732L;

    public Boutique(String id,String name)
    {
        this.id = id;
        this.nom = name;
    }

}
