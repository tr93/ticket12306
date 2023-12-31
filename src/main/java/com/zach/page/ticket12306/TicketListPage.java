package com.zach.page.ticket12306;

import java.util.List;
import java.util.Map;

import com.zach.model.TicketInfo;
import com.zach.page.AbstractPageObject;
import com.zach.util.TrainUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

/**
 * @author : zw35
 */
@Slf4j
public class TicketListPage extends TicketPageObject {

    //[not(@datatran)] 过滤下拉显示票价的tr
    @FindAll({@FindBy(xpath = "//*[@id=\"queryLeftTable\"]/tr[not(@datatran)]")})
    public List<WebElement> ticketList;


    public void predetermine(TicketInfo ticketData) throws InterruptedException {
        while (isNotTicket()) {
            log.info("当前无票，需要刷新页面");
            getDriver().navigate().refresh();
        }
        while (isNotSaleTime()) {
            log.info("还没到售卖时间");
            Thread.sleep(500);
            getDriver().navigate().refresh();
        }
        String seatType = "";
        for (WebElement webElement : ticketList) {
            /*
                C762
                复
                成都东
                广安南
                06:43
                09:12
                02:29
                当日到达
                -- 有 有 -- -- -- -- -- -- 有 -- 预订
             */

            String text = webElement.getText();
            String[] split = text.split("\n");
            String trainNumber = split[0];
            String[] level = ticketData.getLevel();
            if (StringUtils.equals(ticketData.getTrainNumber(), trainNumber)) {
                String seat = split[split.length - 1];
                Map<String, Boolean> stringBooleanMap = TrainUtil.parseSeat(split);
                for (int i = 0; i < ticketData.getLevel().length; i++) {
                    if (stringBooleanMap.getOrDefault(level[i], false)) {
                        seatType = level[i];
                        WebElement element = webElement.findElement(By.cssSelector("td.no-br > a"));
                        element.click();
                        return;
                    }
                }
            }
        }
        if (StringUtils.isEmpty(seatType)) {
            throw new RuntimeException("无余票");
        }
        this.check();
    }

    private boolean isNotSaleTime() {
        return this.ticketList.get(0).getText().contains("售卖");
    }

    private boolean isNotTicket() {
        try {
            WebElement noTicket = getDriver().findElement(By.xpath("//*[@id=\"no_filter_ticket_6\"]"));
            WebElement fast = getDriver().findElement(By.xpath("//*[@id=\"no_filter_ticket_2\"]"));
            if (noTicket.isDisplayed() || fast.isDisplayed()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
