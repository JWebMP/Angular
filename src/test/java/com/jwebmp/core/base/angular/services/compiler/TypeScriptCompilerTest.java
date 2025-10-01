package com.jwebmp.core.base.angular.services.compiler;

import com.jwebmp.core.Page;
import com.jwebmp.core.base.angular.ProductDetail;
import com.jwebmp.core.base.angular.ProductDetailPart;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.modules.directives.OnClickListenerDirective;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeScriptCompilerTest
{
    private TypeScriptCompiler compiler;
    private TypeScriptCodeGenerator codeGenerator;
    private TypeScriptFileManager fileManager;
    private INgApp<?> app;

    // Mock app class with NgApp annotation for testing
    @NgApp(value = "test-app", bootComponent = ProductDetail.class)
    public static class TestApp extends NGApplication<TestApp>
    {
        public TestApp()
        {
            super();
        }
    }

    @BeforeEach
    public void setup()
    {
        app = new TestApp();
        codeGenerator = new TypeScriptCodeGenerator(app);
        TypeScriptCodeValidator codeValidator = new TypeScriptCodeValidator();
        fileManager = new TypeScriptFileManager(app, codeGenerator, codeValidator);
        compiler = new TypeScriptCompiler(app);
    }

    @Test
    public void testProductDetailComponent() throws IOException
    {
        // Create the ProductDetail component
        ProductDetail productDetail = new ProductDetail();
        IComponent.app.set(app);
        productDetail.toString(0);
        // Generate TypeScript for the component
        String generatedTs = codeGenerator.generateTypeScriptForComponent(productDetail);

        // Expected TypeScript from ProductDetailTest
        String expectedTs = """
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

        // Print the generated TypeScript for debugging
        System.out.println("[DEBUG_LOG] Generated TypeScript for ProductDetail:");
        System.out.println("[DEBUG_LOG] " + generatedTs);

        // Compare the generated TypeScript with the expected TypeScript
        assertEquals(expectedTs, generatedTs, "Generated TypeScript for ProductDetail does not match expected output");

        // Write the component to a file
        File file = fileManager.writeComponentToFile(productDetail, true);
        assertTrue(file.exists(), "Component file was not created");

        // Read the file content and compare with expected TypeScript
        String fileContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedTs, fileContent, "File content does not match expected TypeScript");
    }

    @Test
    public void testOnClickListenerDirective() throws IOException
    {
        // Create the OnClickListenerDirective
        OnClickListenerDirective directive = new OnClickListenerDirective();
        IComponent.app.set(app);

        // Generate TypeScript for the directive
        String generatedTs = codeGenerator.generateTypeScriptForComponent(directive);

        // Expected TypeScript from OnClickListenerDirectiveTest
        String expectedTs = """
                import {HostListener} from '@angular/core';
                import {Directive} from '@angular/core';
                import {ElementRef} from '@angular/core';
                import {Input} from '@angular/core';
                import {OnInit} from '@angular/core';
                import {OnDestroy} from '@angular/core';
                import {EventBusService} from '../../../client/services/EventBusService/EventBusService';
                import {inject} from '@angular/core';
                @Directive({
                	selector:'[clickClassName]',
                	standalone:true,
                	providers:[]
                })
                export class OnClickListenerDirective
                {
                	readonly eventBusService = inject(EventBusService);
                	@Input('clickClassName') clickClassName! : string;
                	@Input("confirm") confirm : boolean = false;
                	@Input("confirmMessage") confirmMessage : string = 'Are you sure?';
                	constructor( private elementRef: ElementRef)
                	{
                	}
                	@HostListener('click', ['$event'])
                	    onClick(event: PointerEvent) {
                	        if(this.confirm)
                	        {
                	            if(confirm(this.confirmMessage))
                	            {
                	                let elementId: string = (event.target as Element).id;
                	                this.eventBusService.send('ajax', {eventClass: this.clickClassName}, 'onClick', event, this.elementRef);
                	            }
                	        }
                	        else {
                	            let elementId: string = (event.target as Element).id;
                	            this.eventBusService.send('ajax', {eventClass: this.clickClassName}, 'onClick', event, this.elementRef);
                	        }
                	    }
                
                }
                """;

        // Compare the generated TypeScript with the expected TypeScript
        assertEquals(expectedTs, generatedTs, "Generated TypeScript for OnClickListenerDirective does not match expected output");

        // Write the directive to a file
        File file = fileManager.writeComponentToFile(directive, true);
        assertTrue(file.exists(), "Directive file was not created");

        // Read the file content and compare with expected TypeScript
        String fileContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedTs, fileContent, "File content does not match expected TypeScript");
    }
}
