package top.fireworkrocket.lookup_kernel.process;

import androidx.lifecycle.viewmodel.ViewModelProviderImpl_androidKt;
import top.fireworkrocket.lookup_kernel.config.DatabaseUtil;
import top.srintelligence.wallpaper_generator.MainActivity;

import java.util.List;
import java.util.Map;

import static top.fireworkrocket.lookup_kernel.process.net.util.URLUtil.removeURLParam;
import static top.fireworkrocket.lookup_kernel.process.net.util.URLUtil.replaceURLParam;
import static top.fireworkrocket.lookup_kernel.process.net.util.URLUtil.addURLParam;
import static top.fireworkrocket.lookup_kernel.process.net.util.URLUtil.parseURLParams;

/**
 * 这里是 API 参数处理器，此类用于编辑、删除和添加 URL 参数。
 *
 * <p>示例用法：</p>
 * <pre>{@code
 * List<String> apiObservableList = new ArrayList<>();
 * String apiUrl = "http://example.com/api?param1=value1&param2=value2";
 *
 * // 编辑参数
 * String newUrl = ApiParamHandler.editParam("param1=value1", "param1=newValue", apiUrl, apiObservableList);
 * System.out.println(newUrl); // 输出: http://example.com/api?param1=newValue&param2=value2
 *
 * // 删除参数
 * newUrl = ApiParamHandler.deleteParam("param2=value2", apiUrl, apiObservableList);
 * System.out.println(newUrl); // 输出: http://example.com/api?param1=value1
 *
 * // 添加参数
 * newUrl = ApiParamHandler.addParam("param3=value3", apiUrl, apiObservableList);
 * System.out.println(newUrl); // 输出: http://example.com/api?param1=value1&param2=value2&param3=value3
 * }</pre>
 */
public class ApiParamHandler {

    static DatabaseUtil databaseUtil = MainActivity.getDatabaseUtil();

    /**
     * 编辑指定 URL 的参数。
     *
     * @param selectedParam 选中的参数
     * @param newParam 新参数
     * @param apiUrl 要编辑的 URL
     * @param apiObservableList 可观察的 URL 列表
     * @return 编辑后的新 URL
     * @throws Exception 如果参数格式错误
     */
    public static String editParam(String selectedParam, String newParam, String apiUrl, List<String> apiObservableList) throws Exception {
        String[] keyValue = newParam.split("=");
        if (keyValue.length != 2) {
            throw new Exception("参数格式错误");
        }

        String newUrl = replaceURLParam(keyValue[0], apiUrl, keyValue[0], keyValue[1]);
        apiObservableList.remove(apiUrl);
        apiObservableList.add(newUrl);
        databaseUtil.replaceItem(apiUrl, newUrl);
        System.out.println("editParam: " + newUrl);
        return newUrl;
    }

    /**
     * 删除指定 URL 的参数。
     *
     * @param selectedParam 选中的参数
     * @param apiUrl 要删除参数的 URL
     * @param apiObservableList 可观察的 URL 列表
     * @return 删除参数后的新 URL
     */
    public static String deleteParam(String selectedParam, String apiUrl, List<String> apiObservableList) {
        String newUrl = removeURLParam(apiUrl, selectedParam.split("=")[0]);
        apiObservableList.remove(apiUrl);
        apiObservableList.add(newUrl);
        databaseUtil.replaceItem(apiUrl, newUrl);
        return newUrl;
    }

    /**
     * 添加参数到指定 URL。
     *
     * @param newParam 新参数
     * @param apiUrl 要添加参数的 URL
     * @param apiObservableList 可观察的 URL 列表
     * @return 添加参数后的新 URL
     * @throws IllegalArgumentException 如果参数格式错误或参数为空
     * @throws IllegalStateException 如果参数已存在
     */
    public static String addParam(String newParam, String apiUrl, List<String> apiObservableList) {
        if (newParam == null || apiUrl == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        String[] keyValue = newParam.split("=");
        if (keyValue.length != 2 || keyValue[0].isEmpty()) {
            throw new IllegalArgumentException("参数格式错误");
        }

        Map<String, String> params = parseURLParams(apiUrl);
        if (params.containsKey(keyValue[0])) {
            throw new IllegalStateException("参数已存在");
        }

        String newUrl = addURLParam(apiUrl, keyValue[0], keyValue[1]);
        apiObservableList.remove(apiUrl);
        apiObservableList.add(newUrl);
        databaseUtil.replaceItem(apiUrl, newUrl);
        return newUrl;
    }

    /**
     * 批量添加参数到指定 URL（不更新观察列表）
     *
     * @param params 要添加的参数 Map
     * @param apiUrl 目标 URL
     * @return 处理后的 URL
     * @throws IllegalArgumentException 参数无效时抛出
     */
    public static String addParamsWithoutObservable(Map<String, String> params, String apiUrl) {
        if (params == null || apiUrl == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        String resultUrl = apiUrl;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("参数键值对不能为空");
            }
            resultUrl = addURLParam(resultUrl, entry.getKey(), entry.getValue());
        }
        return resultUrl;
    }

}