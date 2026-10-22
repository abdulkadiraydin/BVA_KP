package com.bvakp.automation.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Yalnızca API çağıran testleri işaretler.
 *
 * <p>İşaretli testlerde tarayıcı açılmaz; ekran görüntüsü, video ve trace oluşmaz.
 * Kanıt, ApiClient'ın Allure'a eklediği istek/yanıt kayıtlarıdır.
 * İşaretli testte page() null döner; sayfa nesnesi oluşturulmamalıdır.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiTesti {
}