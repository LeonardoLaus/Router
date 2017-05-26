package cherry.android.router.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import cherry.android.router.annotations.Interceptor;
import cherry.android.router.annotations.Route;
import cherry.android.router.compiler.generate.InterceptorGenerator;
import cherry.android.router.compiler.generate.RoutePickerGenerator;

import static cherry.android.router.compiler.common.Values.ACTIVITY_CLASS_NAME;
import static cherry.android.router.compiler.common.Values.FRAGMENT_CLASS_NAME;
import static cherry.android.router.compiler.common.Values.OPTION_MODULE_NAME;
import static cherry.android.router.compiler.common.Values.SUPPORT_V4_FRAGMENT_CLASS_NAME;

/**
 * Created by Administrator on 2017/5/24.
 */
@AutoService(Processor.class)
public class RouterProccessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * add in Module build.gradle
     * <p>
     * javaCompileOptions {
     * annotationProcessorOptions {
     * arguments = [ModuleName: project.getName()]
     * }
     * }
     */
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new HashSet<>();
        supportedOptions.add(OPTION_MODULE_NAME);
        return supportedOptions;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            annotationTypes.add(annotation.getCanonicalName());
        }
        return annotationTypes;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new HashSet<>();
        annotations.add(Route.class);
        annotations.add(Interceptor.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            parseRouterTarget(roundEnv);
//            throw new IllegalArgumentException("111");
        }
        return false;
    }

    private void parseRouterTarget(RoundEnvironment roundEnv) {
        String moduleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
        if (moduleName == null) {
            error(null, "java compiler option %s Not Found!", OPTION_MODULE_NAME);
            return;
        }
        try {
            parseRoute(upperCase(moduleName), roundEnv).generate().writeTo(processingEnv.getFiler());
            parseInterceptor(upperCase(moduleName), roundEnv).generate().writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private RoutePickerGenerator parseRoute(String moduleName, RoundEnvironment roundEnv) {
        RoutePickerGenerator routeGenerator = new RoutePickerGenerator(moduleName);
        for (Element element : roundEnv.getElementsAnnotatedWith(Route.class)) {
            if (checkInvalid(element)) {
                routeGenerator.addRoute(new RouteClass((TypeElement) element,
                        processingEnv.getElementUtils(),
                        processingEnv.getTypeUtils()));
            }
        }
        return routeGenerator;
    }

    private InterceptorGenerator parseInterceptor(String moduleName, RoundEnvironment roundEnv) {
        InterceptorGenerator interceptorGenerator = new InterceptorGenerator(moduleName);
        for (Element element : roundEnv.getElementsAnnotatedWith(Interceptor.class)) {
            interceptorGenerator.addInterceptor(new InterceptorClass((TypeElement) element));
        }
        return interceptorGenerator;
    }

    private boolean checkInvalid(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "Annotation %s should be used with Class");
            return false;
        }
        if (!isSubtype(element, ACTIVITY_CLASS_NAME)
                && !isSubtype(element, FRAGMENT_CLASS_NAME)
                && !isSubtype(element, SUPPORT_V4_FRAGMENT_CLASS_NAME)) {
            error(element, "%s must be a Activity or Fragment", element.getSimpleName().toString());
            return false;
        }
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            error(element, "Abstract not support: %s", element.getSimpleName().toString());
            return false;
        }
        return true;
    }

    private boolean isSubtype(Element element, String type) {
        return processingEnv.getTypeUtils().isSubtype(element.asType(),
                processingEnv.getElementUtils().getTypeElement(type).asType());
    }

    private void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }

    private String upperCase(CharSequence sequence) {
        return sequence.length() == 0 ? "" :
                "" + Character.toUpperCase(sequence.charAt(0))
                        + sequence.subSequence(1, sequence.length());
    }
}