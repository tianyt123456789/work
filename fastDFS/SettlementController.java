package com.ysl.ts.business.profit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysl.ts.business.common.BaseController;
import com.ysl.ts.business.common.BusinessDic;
import com.ysl.ts.business.profit.model.Settlement;
import com.ysl.ts.business.profit.service.SettlementDetailService;
import com.ysl.ts.business.profit.service.SettlementService;
import com.ysl.ts.business.profit.vo.SettlementDetailVO;
import com.ysl.ts.business.profit.vo.SettlementVO;
import com.ysl.ts.common.appEnum.AppEnumBase;
import com.ysl.ts.common.basicData.BasicDataProvider;
import com.ysl.ts.common.excelExporter.ExcelPoiUtil;
import com.ysl.ts.common.excelExporter.ReportExcelModelVO;
import com.ysl.ts.common.postFileClient.FileView;
import com.ysl.ts.common.postFileClient.PostFileUtil;
import com.ysl.ts.common.postFileClient.UploadFileReq;
import com.ysl.ts.common.serviceModel.ServiceResponse;
import com.ysl.ts.core.model.base.ts_base.TsAgentModel;
import com.ysl.ts.core.model.base.ts_base.TsUserModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author xieyg
 * @Date 2018年12月29日
 */
@Controller
@RequestMapping("settlement")
public class SettlementController extends BaseController {
    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementDetailService settlementDetailService;

    /**
     * 初始化结算单
     *
     * @param
     * @return
     */
    @RequestMapping("/tosettlemen")
    public String tosettlemen(Page page, SettlementVO settlement, Model model) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        settlement.setEndTimeCon(format.format(date));
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        date = c.getTime();
        settlement.setBeginTimeCon(format.format(date));
        Page<Settlement> listPage = settlementService.findSettlementByCondition(page, settlement);
        Map<String, String> statusList = BusinessDic.getDicByDicType(BusinessDic.ServiceDicEnum.status);
        model.addAttribute("statusList", statusList);
        model.addAttribute("settlement", settlement);
        model.addAttribute("page", listPage);
        return "/profit/Settlement";
    }

    /**
     * 查询结算单
     *
     * @param
     * @return
     */
    @RequestMapping("/list")
    public String settlementList(Page page, SettlementVO settlement, Model model) {
        Page<Settlement> listPage = settlementService.findSettlementByCondition(page, settlement);
        List<TsAgentModel> suppliers = BasicDataProvider.listSuppliers(AppEnumBase.ProjectType.IFlight);
        Map<String, String> statusList = BusinessDic.getDicByDicType(BusinessDic.ServiceDicEnum.status);
        model.addAttribute("statusList", statusList);
        model.addAttribute("settlement", settlement);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("page", listPage);
        return "/profit/Settlement";
    }

    /**
     * 查看结算单
     *
     * @param
     * @return
     */
    @RequestMapping("/toedit")
    public String toSettlement(@NotEmpty String id, String tab, Model model) {
        Settlement settlementById = settlementService.findSettlementById(id);
        List<TsAgentModel> suppliers = BasicDataProvider.listSuppliers(AppEnumBase.ProjectType.IFlight);
        Map<String, String> statusList = BusinessDic.getDicByDicType(BusinessDic.ServiceDicEnum.status);
        model.addAttribute("statusList", statusList);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("settlement", settlementById);
        model.addAttribute("settlementId", id);
        model.addAttribute("tab", tab);
        return "/profit/SettlementDetail";
    }


    /**
     * 通过主键作废结算单
     *
     * @param
     * @return
     */
    @RequestMapping("/cancelSettlement")
    @ResponseBody
    public ServiceResponse<Boolean> cancelSettlement(@NotEmpty String id, String tab, Model model) {
        TsUserModel tsUserModel = getLoginUser();
        ServiceResponse<Boolean> response = settlementService.cancelSettlement(id, tsUserModel);
        Map<String, String> statusList = BusinessDic.getDicByDicType(BusinessDic.ServiceDicEnum.status);
        model.addAttribute("statusList", statusList);
        model.addAttribute("settlementId", id);
        model.addAttribute("tab", tab);
        return response;
    }

    /**
     * 执行结算操作
     *
     * @param
     * @return
     */
    @RequestMapping("/carryOutSettlement")
    @ResponseBody
    public ServiceResponse<Boolean> carryOutSettlement(@NotEmpty String id, String tab, Model model) {
        TsUserModel tsUserModel = getLoginUser();
        ServiceResponse<Boolean> response = settlementService.carryOutSettlement(id, tsUserModel);
        Map<String, String> statusList = BusinessDic.getDicByDicType(BusinessDic.ServiceDicEnum.status);
        model.addAttribute("statusList", statusList);
        model.addAttribute("settlementId", id);
        model.addAttribute("tab", tab);
        return response;
    }

    @RequestMapping("/fileUpload")
    @ResponseBody
    public ServiceResponse<List<FileView>> fileUpload(@RequestParam("file") MultipartFile[] files, @RequestParam("importVenderName") String importVenderName, @RequestParam("businessCode") String businessCode, String extensions) throws Exception {
        TsUserModel tsUserModel = getLoginUser();
        String businessId = UUID.randomUUID().toString().replaceAll("-", "");
        InputStream resStream = files[0].getInputStream();
        //存储文件流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = resStream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        //文件流1
        InputStream streamOne = new ByteArrayInputStream(baos.toByteArray());
        InputStream streamTwo = new ByteArrayInputStream(baos.toByteArray());
        MultipartFile multipartFile = new MockMultipartFile(files[0].getOriginalFilename(), files[0].getOriginalFilename(), "", streamOne);
        MultipartFile multipartFileTwo = new MockMultipartFile(files[0].getOriginalFilename(), files[0].getOriginalFilename(), "", streamTwo);
        MultipartFile[] multipartFiles;    // 声明要创建的对象数组
        multipartFiles = new MultipartFile[1];   // 创建对象数组，为对象数组开辟空间
        multipartFiles[0] = multipartFile;
        UploadFileReq uploadFileReq = new UploadFileReq();
        uploadFileReq.setBusinessCode(businessCode);
        uploadFileReq.setBusinessId(businessId);
        uploadFileReq.setUploadUser(tsUserModel.getId().toString());
        ServiceResponse<List<FileView>> response = PostFileUtil.postMultipartFile(uploadFileReq, multipartFiles, extensions);
        Settlement settlement = new Settlement();
        settlement.setVenderName(importVenderName);
        settlement.setFileId(response.getResData().get(0).getFileId());
        Settlement settlementResult = settlementService.saveSettlement(settlement, tsUserModel);
        settlementService.runImport(multipartFileTwo, tsUserModel, settlementResult);
        return response;
    }


    /**
     * 充值记录加一个报表导出功能 xieyg
     *
     * @param
     * @return
     */
    @RequestMapping("/downloadExcel")
    @ResponseBody
    public ServiceResponse<String> downloadExcel(String fileId) throws IOException {
        String url = PostFileUtil.getDownloadUrl(fileId);
        return new ServiceResponse<>(url);
    }

    /**
     * 充值记录加一个报表导出功能 xieyg
     *
     * @param
     * @return
     */
    @RequestMapping("/downloadSettlement")
    @ResponseBody
    public String downloadSettlement(String settlementId) throws Exception {
        List<SettlementDetailVO> settlementDetailList = settlementDetailService.findSettlementDetailBySettlementId(settlementId);
        List<ReportExcelModelVO> reportExcelModelVOList = new ArrayList<ReportExcelModelVO>();
        String[][] titles = new String[][]{{"结算单编号", "settlementNo", "20"}, {"供应名称", "venderName", "20"}};

        for (int i = 0; i < titles.length; i++) {
            ReportExcelModelVO remv = new ReportExcelModelVO();
            remv.setTitleName(titles[i][0]);
            remv.setKey(titles[i][1]);
            remv.setWidth(256 * Integer.parseInt(titles[i][2]) + 184);
            reportExcelModelVOList.add(remv);
        }
        Workbook wb = ExcelPoiUtil.writeExcel(reportExcelModelVOList, settlementDetailList);
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        String file_name = new String("结算单报表.xlsx".getBytes("GB2312"), "ISO-8859-1");
        response.setHeader("Content-Disposition", "attachment;filename=" + file_name);
        response.setContentType("application/msexcel");
        OutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
        return null;
    }


}

