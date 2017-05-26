package cherry.android.router;

import cherry.android.router.annotations.Interceptor;
import cherry.android.router.api.RouteMeta;
import cherry.android.router.api.intercept.IInterceptor;
import cherry.android.router.api.utils.Logger;

/**
 * Created by Administrator on 2017/5/26.
 */
@Interceptor(value = "route1", priority = 0)
public class Route1Interceptor implements IInterceptor {
    @Override
    public boolean intercept(RouteMeta routeMeta) {
        Logger.i("Test", "intercept in 1 :" + routeMeta.getDestination());
        return false;
    }
}
