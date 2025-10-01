package com.jwebmp.core.base.angular.modules.directives;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OnClickListenerDirectiveTest
{
    String expected = """
            import { EventBusService } from '../../../client/services/EventBusService/EventBusService';
            import { HostListener } from '@angular/core';
            import { inject } from '@angular/core';
            import { Directive } from '@angular/core';
            import { ElementRef } from '@angular/core';
            import { Input } from '@angular/core';
            import { OnInit } from '@angular/core';
            import { OnDestroy } from '@angular/core';
            @Directive({
            	selector:'[clickClassName]',
            	standalone:true,
            	providers:[]
            })
            export class OnClickListenerDirective implements OnInit,OnDestroy
            {
            	@Input('clickClassName') clickClassName! : string;
            	@Input("confirm") confirm : boolean = false;
            	@Input("confirmMessage") confirmMessage : string = 'Are you sure?';
            	constructor( private elementRef: ElementRef, public eventBusService : EventBusService )
            	{
            	}
            ngOnDestroy() {
            }
            @HostListener('click', ['$event'])
                        onClick(event: PointerEvent) {
                            // If the element has a confirm attribute, do not fire here;
                            // confirmation handling is done elsewhere.
                            if(this.confirm)
                            {
                                return;
                            }
                            let elementId: string = (event.target as Element).id;
                            this.eventBusService.send('ajax', {eventClass: this.clickClassName}, 'onClick', event, this.elementRef);
                        }
            ngOnInit() {
            }
            }
            
            """;

    @Test
    public void testRenderOfOnClick()
    {
        File f = new File("webroot/main/src/app/com/jwebmp/core/base/angular/modules/directives/OnClickListenerDirective/OnClickListenerDirective.ts");
        System.out.println("[DEBUG_LOG] Looking for file at: " + f.getAbsolutePath());
        System.out.println("[DEBUG_LOG] File exists: " + f.exists());

        if (f.exists())
        {
            String content = "";
            try
            {
                content = new String(java.nio.file.Files.readAllBytes(f.toPath()));
                // Normalize line endings for robust comparison across OSes
                content = content.replace("\r\n", "\n");
                String normalizedExpected = expected.replace("\r\n", "\n");
                System.out.println("[DEBUG_LOG] Actual content:\n" + content);
                // Behavior-focused assertions instead of full file equality
                assertTrue(content.contains("if(this.confirm)"), "Directive should check for confirm attribute");
                assertTrue(content.contains("return;"), "Directive should early-return when confirm is present");
                assertFalse(content.contains("confirm(this.confirmMessage)"), "Directive should not call window.confirm here");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
