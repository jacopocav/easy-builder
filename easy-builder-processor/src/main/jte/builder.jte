@import java.util.Collection
@import java.time.OffsetDateTime
@import com.github.jacopocav.builder.annotation.GeneratedBuilder
@import com.github.jacopocav.builder.internal.option.InterpolatedOptions
@import com.github.jacopocav.builder.processing.type.TypeRegistry
@import gg.jte.support.ForSupport
@import com.github.jacopocav.builder.internal.template.Member
@import com.github.jacopocav.builder.internal.template.MetadataAnnotation

@param String packageName
@param TypeRegistry typeRegistry
@param String processorName
@param OffsetDateTime creationTimestamp
@param Collection<MetadataAnnotation> metadataAnnotations
@param String className
@param Collection<Member> members
@param InterpolatedOptions options
@param String targetClassName
@param String enclosingClassName
@param String staticCreatorMethod
@param boolean generateCopyFactoryMethod

package ${packageName};

<%-- imports --%>
@for (var type: typeRegistry.getSafeImports())
    import ${type};
@endfor


<%-- annotations --%>
@${typeRegistry.getUsageName(javax.annotation.processing.Generated.class)}(
    value = "${processorName}",
    date = "${creationTimestamp.toString()}")
!{var generatedBuilder = typeRegistry.getUsageName(GeneratedBuilder.class);}
@${generatedBuilder}(
    targetClass = ${targetClassName}.class,
@for (var metadata: ForSupport.of(metadataAnnotations))
    ${metadata.get().name()} = ${metadata.get().value()}@if(!metadata.isLast()),@endif
@endfor)
<%-- builder class --%>
public class ${className} {
    <%-- private fields --%>
    @for(var member: members)
        private ${member.type()} ${member.name()};
    @endfor

    <%-- private constructor --%>
    private ${className}() {}

    <%-- static factory method --%>
    public static ${className} ${options.staticFactoryName()}() {
        return new ${className}();
    }

    <%-- static from method --%>
    @if(generateCopyFactoryMethod)
        public static ${className} ${options.copyFactoryMethodName()}(${targetClassName} other) {
            return ${options.staticFactoryName()}()
            @for(var member: members)
                .${member.setterName()}(other.${member.getterName()})
            @endfor;
        }
    @endif

    <%-- build method --%>
    public ${targetClassName} ${options.buildMethodName()}() {
        return @if(staticCreatorMethod.isEmpty())
            new ${targetClassName}
        @else
            ${enclosingClassName}.${staticCreatorMethod}
        @endif(
            @for(var member: ForSupport.of(members))
                ${member.get().name()}
                @if (!member.isLast()), @endif
            @endfor
        );
    }
    <%-- setters --%>
    @for (var member: members)
        public ${className} ${member.setterName()}(${member.type()} ${member.name()}) {
            this.${member.name()} = ${member.name()};
            return this;
        }
    @endfor
}