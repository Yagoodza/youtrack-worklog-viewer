package de.pbauerochse.worklogviewer.youtrack.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import de.pbauerochse.worklogviewer.util.ExceptionUtil;
import de.pbauerochse.worklogviewer.util.JacksonUtil;
import de.pbauerochse.worklogviewer.util.SettingsUtil;
import de.pbauerochse.worklogviewer.youtrack.createreport.request.CreateReportRequestEntity;
import de.pbauerochse.worklogviewer.youtrack.createreport.response.ReportDetailsResponse;
import de.pbauerochse.worklogviewer.youtrack.domain.GroupByCategory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick Bauerochse
 * @since 15.10.15
 */
public abstract class YouTrackConnectorBase implements YouTrackConnector {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected abstract CloseableHttpClient performLoginIfNecessary(HttpClientBuilder clientBuilder, List<Header> requestHeaders) throws Exception;

    protected CloseableHttpClient getLoggedInClient() throws Exception {
        HttpClientBuilder defaultClientBuilder = getDefaultClientBuilder();
        return performLoginIfNecessary(defaultClientBuilder, getDefaultHeaders());
    }

    protected HttpClientBuilder getDefaultClientBuilder() {
        RequestConfig config = RequestConfig
                .custom()
                .setConnectTimeout(10 * 1000)               // 10 s
                .setConnectionRequestTimeout(10 * 1000)     // 10 s
                .build();

        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

        return HttpClients
                .custom()
                .setDefaultRequestConfig(config)
                .setRoutePlanner(routePlanner);
    }

    private List<Header> getDefaultHeaders() {
        List<Header> headerList = new ArrayList<>();
        headerList.add(new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36"));
        headerList.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
        headerList.add(new BasicHeader("Accept-Language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4"));
        return headerList;
    }

    @Override
    public List<GroupByCategory> getPossibleGroupByCategories() throws Exception {

        CloseableHttpClient client = getLoggedInClient();

        String getGroupByCategoriesUrl = buildYoutrackApiUrl("reports/timeReports/possibleGroupByCategories");

        HttpGet request = new HttpGet(getGroupByCategoriesUrl);
        request.addHeader("Accept", "application/json, text/plain, */*");

        try (CloseableHttpResponse httpResponse = client.execute(request)) {
            if (!isValidResponseCode(httpResponse.getStatusLine())) {
                LOGGER.warn("Fetching groupBy categories from {} failed: {}", getGroupByCategoriesUrl, httpResponse.getStatusLine().getReasonPhrase());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                throw ExceptionUtil.getIllegalStateException("exceptions.main.worker.groupbycategories", httpResponse.getStatusLine().getReasonPhrase(), httpResponse.getStatusLine().getStatusCode());
            }

            String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
            LOGGER.debug("Received JSON groupByCategories response {}", jsonResponse);

            StringReader response = new StringReader(jsonResponse);
            return JacksonUtil.parseValue(response, new TypeReference<List<GroupByCategory>>() {});
        }
    }

    @Override
    public ReportDetailsResponse createReport(CreateReportRequestEntity requestEntity) throws Exception {

        CloseableHttpClient client = getLoggedInClient();

        LOGGER.debug("Creating temporary timereport");
        String createReportUrl = buildYoutrackApiUrl("current/reports");

        HttpPost createReportRequest = new HttpPost(createReportUrl);

        // request body
        String requestEntityAsString = JacksonUtil.writeObject(requestEntity);

        createReportRequest.setEntity(new StringEntity(requestEntityAsString, "utf-8"));
        createReportRequest.addHeader("Content-Type", "application/json;charset=UTF-8");

        // create report
        try (CloseableHttpResponse response = client.execute(createReportRequest)) {
            if (!isValidResponseCode(response.getStatusLine())) {
                LOGGER.error("Creating temporary timereport failed: {}", response.getStatusLine().getReasonPhrase());
                throw ExceptionUtil.getIllegalStateException("exceptions.main.worker.creatingreport", response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
            }

            String responseJson = EntityUtils.toString(response.getEntity());

            if (StringUtils.isBlank(responseJson)) {
                LOGGER.warn("Response from youtrack was blank");
                throw ExceptionUtil.getIllegalStateException("exceptions.main.worker.blankresponse");
            }

            return JacksonUtil.parseValue(new StringReader(responseJson), ReportDetailsResponse.class);
        }
    }

    @Override
    public ReportDetailsResponse getReportDetails(String reportId) throws Exception {

        CloseableHttpClient client = getLoggedInClient();

        String reportUrlTemplate = buildYoutrackApiUrl("current/reports/%s");
        LOGGER.debug("Fetching report details from {}", reportUrlTemplate);

        HttpGet reportDetailsRequest = new HttpGet(String.format(reportUrlTemplate, reportId));

        try (CloseableHttpResponse httpResponse = client.execute(reportDetailsRequest)) {
            if (!isValidResponseCode(httpResponse.getStatusLine())) {
                LOGGER.warn("Fetching report details from {} failed: {}", reportUrlTemplate, httpResponse.getStatusLine().getReasonPhrase());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                throw ExceptionUtil.getIllegalStateException("exceptions.main.worker.reportstatus", httpResponse.getStatusLine().getReasonPhrase(), httpResponse.getStatusLine().getStatusCode());
            }

            String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
            LOGGER.debug("Received JSON response {}", jsonResponse);
            return JacksonUtil.parseValue(new StringReader(jsonResponse), ReportDetailsResponse.class);
        }
    }

    @Override
    public ByteArrayInputStream downloadReport(String reportId) throws Exception {

        CloseableHttpClient client = getLoggedInClient();

        String downloadReportUrlTemplate = buildYoutrackApiUrl("current/reports/%s/export");

        HttpGet request = new HttpGet(String.format(downloadReportUrlTemplate, reportId));
        return client.execute(request, response -> {
            HttpEntity entity = response.getEntity();
            ByteArrayInputStream reportDataInputStream = null;

            try {
                byte[] reportBytes = EntityUtils.toByteArray(entity);
                reportDataInputStream = new ByteArrayInputStream(reportBytes);
            } finally {
                ((CloseableHttpResponse) response).close();
            }

            if (!isValidResponseCode(response.getStatusLine())) {
                // invalid response code
                int statusCode = response.getStatusLine().getStatusCode();
                throw ExceptionUtil.getIllegalStateException("exceptions.main.worker.statuscode", response.getStatusLine().getReasonPhrase(), statusCode);
            }

            return reportDataInputStream;
        });
    }

    @Override
    public void deleteReport(String reportId) throws Exception {

        CloseableHttpClient client = getLoggedInClient();

        String reportUrlTemplate = buildYoutrackApiUrl("current/reports/%s");
        String deleteReportUrl = String.format(reportUrlTemplate, reportId);

        LOGGER.debug("Deleting temporary report using url {}", deleteReportUrl);
        HttpDelete deleteRequest = new HttpDelete(deleteReportUrl);

        try (CloseableHttpResponse response = client.execute(deleteRequest)) {
            EntityUtils.consumeQuietly(response.getEntity());

            if (!isValidResponseCode(response.getStatusLine())) {
                LOGGER.warn("Could not delete temporary report using url {}: {}", reportUrlTemplate, response.getStatusLine().getReasonPhrase());
                throw ExceptionUtil.getIllegalStateException("exceptions.main.worker.deletereport", response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
            }
        }
    }

    protected String buildYoutrackApiUrl(String path) {
        SettingsUtil.Settings settings = SettingsUtil.loadSettings();
        StringBuilder finalUrl = new StringBuilder(StringUtils.trim(settings.getYoutrackUrl()));

        if (!StringUtils.endsWith(settings.getYoutrackUrl(), "/") && !StringUtils.startsWith(path, "/")) {
            finalUrl.append('/');
        }

        if (!StringUtils.endsWith(finalUrl, "rest/")) {
            finalUrl.append("rest/");
        }

        return finalUrl.append(path).toString();
    }

    protected static boolean isValidResponseCode(StatusLine statusLine) {
        if (statusLine == null) throw ExceptionUtil.getIllegalArgumentException("exceptions.main.worker.nullstatus");
        int statusCode = statusLine.getStatusCode();

        return (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES);
    }
}
