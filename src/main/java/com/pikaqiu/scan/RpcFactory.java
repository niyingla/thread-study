package com.pikaqiu.scan;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @program: demo
 * @description:
 * @author: xiaoye
 * @create: 2019-09-09 11:50
 **/

//@Slf4j
//public class RpcFactory implements InvocationHandler {

//  private SpringCloudClient springCloudClient = SpringBeanUtils.getBean(SpringCloudClient.class);
//  private ApiHSHttpService hsHttpService = SpringBeanUtils.getBean(ApiHSHttpService.class);
//
//  @Override
//  public Object invoke(Object proxy, Method method, Object[] args) {
//    OpenBaseModel openBaseModel = null;
//    method.setAccessible(true);
//    //1 记录日志
//    log.info("客户请求：{}.{}", method.getDeclaringClass().getName(), method.getName());
//    log.info("请求参数是：{}", StringUtils.argsArray2String(args));
//
//    //2 判断请求 查询 还是修改 修改请求数据写库
//    RpcCase annotation = method.getAnnotation(RpcCase.class);
//    if (annotation.isQuery().equals(isQuery.FALSE) && annotation.DataType() != Object.class) {
//      //记录请求数据
//      openBaseModel = toDbData(args, annotation);
//    }
//    //3 请求动作
//    Object returnObj = sendRequest(method, args, annotation);
//    if (annotation.isQuery().equals(isQuery.FALSE) && annotation.DataType() != Object.class) {
//      log.info("响应结果是是：{}", JSON.toJSONString(returnObj));
//      //记录响应数据
//      resultToDb(openBaseModel, annotation, returnObj);
//    }
//    //4 响应数据
//    return returnObj;
//  }
//
//  /**
//   * 发送远程请求
//   *
//   * @param method
//   * @param args
//   * @param annotation
//   * @return
//   */
//  private Object sendRequest(Method method, Object[] args, RpcCase annotation) {
//    Object returnObj;
//    if (annotation.queryUrl().equals(FOREIGN_CONSTANS.HS_URL)) {
//      //发起恒生请求
//      String result = hsHttpService.doHttp(annotation.functionId().getFunctionId(), (String) args[0]);
//      Class<?> returnType = method.getReturnType();
//      returnObj = JSON.parseObject(result, returnType);
//    } else {
//      //内部服务请求
//      String adapterParams = StringUtils.adapterParams((String) args[0]);
//      String result = springCloudClient.post(annotation.queryUrl(), adapterParams);
//      Class<?> returnType = method.getReturnType();
//      returnObj = JSON.parseObject(result, returnType);
//    }
//    return returnObj;
//  }
//
//  /**
//   * 记录请求数据到数据库
//   *
//   * @param args
//   * @param annotation
//   */
//  private OpenBaseModel toDbData(Object[] args, RpcCase annotation) {
//    BaseDAO baseDao = getBaseDao(annotation);
//    OpenBaseModel parseObject = (OpenBaseModel) JSON.parseObject((String) args[0], annotation.DataType());
//    baseDao.save(parseObject);
//    return parseObject;
//  }
//
//  /**
//   * 记录响应数据
//   * @param baseModel
//   * @param annotation
//   * @param result
//   */
//  private void resultToDb(OpenBaseModel baseModel, RpcCase annotation, Object result) {
//    BaseDAO baseDao = getBaseDao(annotation);
//    if (result instanceof HSResultVO) {
//      HSResultVO hsResultVO = (HSResultVO) result;
//      baseModel.setRpcResult(hsResultVO.getRsp_content());
//      baseModel.setErrorMsg(hsResultVO.getError_msg());
//    } else if (result instanceof ResultData) {
//      ResultData resultData = (ResultData) result;
//      baseModel.setRpcResult(JSON.toJSONString(resultData.getData()));
//      baseModel.setErrorMsg(resultData.getErrorMsg());
//    } else {
//      baseModel.setRpcResult(JSON.toJSONString(result));
//    }
//    baseDao.save(baseModel);
//  }
//
//  /**
//   * 获取dao层类
//   * @param annotation
//   * @return
//   */
//  private BaseDAO getBaseDao(RpcCase annotation) {
//    String daoName = StringUtils.lowerFirstDao(annotation.DataType().getSimpleName());
//    return (BaseDAO) SpringBeanUtils.getBean(daoName);
//  }

//}
