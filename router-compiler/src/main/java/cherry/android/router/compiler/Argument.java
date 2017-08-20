package cherry.android.router.compiler;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import cherry.android.router.annotations.Extra;

/**
 * Created by Administrator on 2017/6/8.
 */

public class Argument {
    private VariableElement mFieldElement;
    private String mKeyName;
    private boolean mNonNull;

    public Argument(Element element) {
        if (!element.getKind().isField())
            throw new IllegalStateException(String.format("Only field can be annotated with @%s",
                    Argument.class.getSimpleName()));
        mFieldElement = (VariableElement) element;
        Extra annotation = mFieldElement.getAnnotation(Extra.class);
        mKeyName = annotation.name();
        mNonNull = annotation.nonNull();
        if (mKeyName.isEmpty())
            mKeyName = getFieldName();
    }

    public TypeMirror asType() {
        return mFieldElement.asType();
    }

    public String getFieldName() {
        return mFieldElement.getSimpleName().toString();
    }

    public TypeName getTypeName() {
        return TypeName.get(mFieldElement.asType());
    }

    public String getKey() {
        return mKeyName;
    }

    public boolean isNonNull() {
        return mNonNull;
    }

}