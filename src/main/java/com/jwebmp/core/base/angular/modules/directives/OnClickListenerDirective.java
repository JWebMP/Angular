package com.jwebmp.core.base.angular.modules.directives;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDirective;

import java.util.List;

@NgDirective(value = "[clickClassName]", standalone = true)
@NgField("@Input('clickClassName') clickClassName! : string;")
@NgImportReference(value = "HostListener", reference = "@angular/core")

@NgImportReference(value = "inject", reference = "@angular/core")

@NgField("@Input(\"confirm\") confirm : boolean = false;")
@NgField("@Input(\"confirmMessage\") confirmMessage : string = 'Are you sure?';")
public class OnClickListenerDirective implements INgDirective<OnClickListenerDirective>
{
    public OnClickListenerDirective()
    {
    }

    @Override
    public List<String> methods()
    {
        List<String> out = INgDirective.super.methods();
        out.add(
                //"ngOnInit() {}\n",
                """
                        \t
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
                        """);

        return out;
    }

}
