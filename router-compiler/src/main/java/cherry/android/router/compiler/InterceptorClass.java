package cherry.android.router.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;

import cherry.android.router.annotations.Interceptor;
import cherry.android.router.compiler.common.Values;
import cherry.android.router.compiler.generate.Generator;

/**
 * Created by Administrator on 2017/5/25.
 */

public class InterceptorClass implements Generator<CodeBlock> {

    private TypeElement mTypeElement;
    private String mName;
    private int mPriority;

    public InterceptorClass(TypeElement typeElement) {
        mTypeElement = typeElement;
        Interceptor interceptor = mTypeElement.getAnnotation(Interceptor.class);
        mName = interceptor.value();
        mPriority = interceptor.priority();
    }

    private TypeName getTypeName() {
        return TypeName.get(mTypeElement.asType());
    }

    @Override
    public CodeBlock generate() {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("param.put($S, new $T($T.class, $S, $L))",
                mName, Values.ROUTE_INTERCEPTOR, getTypeName(), mName, mPriority);
        return codeBuilder.build();
    }

}
