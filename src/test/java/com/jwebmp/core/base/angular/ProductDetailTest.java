package com.jwebmp.core.base.angular;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ProductDetailTest
{
    String expected = """
            import {Component} from '@angular/core';
            import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
            import {inject} from '@angular/core';
            import {RouterModule} from '@angular/router';
            import {RouterOutlet} from '@angular/router';
            import {ProductDetailPart} from '../ProductDetailPart/ProductDetailPart';
            @Component({
            	selector:'product-detail',
            	templateUrl:'./ProductDetail.html',
            	styles: [],
            	styleUrls:['./ProductDetail.scss'
            ],
            	viewProviders:[],
            	animations:[],
            	providers:[],
            	schemas:[CUSTOM_ELEMENTS_SCHEMA],
            	preserveWhitespaces:true,
            	host:{},
            	imports:[
            		RouterModule,
            		RouterOutlet,
            		ProductDetailPart,
            ],
            	standalone:true
            })
            export class ProductDetail
            {
            }
            """;

    @Test
    public void testRenderOfOnClick()
    {
        File f = new File("webroot/main/src/app/com/jwebmp/core/base/angular/ProductDetail/ProductDetail.ts");
        if (f.exists())
        {
            String content = "";
            try
            {
                content = new String(java.nio.file.Files.readAllBytes(f.toPath()));
                System.out.println("[DEBUG_LOG] Actual content:\n" + content);
                System.out.println("[DEBUG_LOG] Expected content:\n" + expected);
                assertEquals(expected, content, "Generated typescript is not valid");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
