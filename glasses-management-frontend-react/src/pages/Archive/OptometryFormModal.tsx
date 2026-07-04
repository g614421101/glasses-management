import React from 'react';
import { Modal, Form, Input, Row, Col, Divider, DatePicker, message } from 'antd';
import dayjs from 'dayjs';
import { optometryAPI, OptometryRecord } from '@/api/optometry';
import { fmtInput } from './formatters';

export type OptoForm = Omit<Partial<OptometryRecord>, 'id' | 'examDate'> & {
  id?: number | null;
  customerId: number;
  examDate?: dayjs.Dayjs;
};

export const buildEmptyOpto = (customerId: number): OptoForm => ({
  id: null,
  customerId,
  odSph: '', odCyl: '', odAxis: '', odVa: '',
  osSph: '', osCyl: '', osAxis: '', osVa: '',
  odPd: '', osPd: '', pdFar: '', pdNear: '', addPower: '',
  optometristName: '',
  examDate: dayjs(),
  remark: '',
});

export const prepareOptoForEdit = (raw: any): OptoForm => ({
  id: raw.id,
  customerId: raw.customerId,
  odSph: fmtInput(raw.odSph),
  odCyl: fmtInput(raw.odCyl),
  odAxis: raw.odAxis,
  odVa: raw.odVa,
  osSph: fmtInput(raw.osSph),
  osCyl: fmtInput(raw.osCyl),
  osAxis: raw.osAxis,
  osVa: raw.osVa,
  odPd: raw.odPd,
  osPd: raw.osPd,
  pdFar: raw.pdFar,
  pdNear: raw.pdNear,
  addPower: fmtInput(raw.addPower),
  optometristName: raw.optometristName,
  examDate: raw.examDate ? dayjs(raw.examDate) : undefined,
  remark: raw.remark,
});

interface Props {
  open: boolean;
  initial: OptoForm | null;
  onClose: () => void;
  onSaved: () => void;
}

const OptometryFormModal: React.FC<Props> = ({ open, initial, onClose, onSaved }) => {
  const [form] = Form.useForm<OptoForm>();
  const [submitting, setSubmitting] = React.useState(false);

  React.useEffect(() => {
    if (open && initial) form.setFieldsValue(initial);
  }, [open, initial, form]);

  const calcPdTotal = () => {
    const od = parseFloat(form.getFieldValue('odPd')) || 0;
    const os = parseFloat(form.getFieldValue('osPd')) || 0;
    if (od > 0 && os > 0) {
      form.setFieldValue('pdFar', (od + os).toFixed(1));
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: any = { ...values, createTime: undefined, updateTime: undefined };
    if (payload.examDate) {
      payload.examDate = dayjs.isDayjs(payload.examDate)
        ? (payload.examDate as dayjs.Dayjs).format('YYYY-MM-DD') + ' 00:00:00'
        : payload.examDate;
    }
    setSubmitting(true);
    try {
      if (payload.id) {
        await optometryAPI.update(payload);
        message.success('更新成功');
      } else {
        await optometryAPI.add(payload);
        message.success('验光单录入成功');
      }
      onClose();
      onSaved();
    } catch (e) {
      // request helper shows errors
    } finally {
      setSubmitting(false);
    }
  };

  const isEdit = !!initial?.id;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={submitting}
      title={isEdit ? '编辑验光单' : '录入验光单'}
      okText="保存"
      cancelText="取消"
      width="min(94vw, 720px)"
      forceRender
    >
      <Form form={form} layout="horizontal" labelCol={{ flex: '80px' }} className="record-form" preserve={false}>
        <Form.Item name="id" hidden><Input /></Form.Item>
        <Form.Item name="customerId" hidden><Input /></Form.Item>

        <h4 className="form-block-title">右眼 (OD)</h4>
        <Row gutter={12}>
          <Col xs={24} sm={12} md={6}><Form.Item label="球镜" name="odSph"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={6}><Form.Item label="柱镜" name="odCyl"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={6}><Form.Item label="轴位" name="odAxis"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={6}><Form.Item label="视力" name="odVa"><Input /></Form.Item></Col>
        </Row>

        <h4 className="form-block-title">左眼 (OS)</h4>
        <Row gutter={12}>
          <Col xs={24} sm={12} md={6}><Form.Item label="球镜" name="osSph"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={6}><Form.Item label="柱镜" name="osCyl"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={6}><Form.Item label="轴位" name="osAxis"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={6}><Form.Item label="视力" name="osVa"><Input /></Form.Item></Col>
        </Row>

        <Divider />

        <Row gutter={12}>
          <Col xs={24} sm={12} md={8}><Form.Item label="右眼瞳距" name="odPd"><Input onBlur={calcPdTotal} /></Form.Item></Col>
          <Col xs={24} sm={12} md={8}><Form.Item label="左眼瞳距" name="osPd"><Input onBlur={calcPdTotal} /></Form.Item></Col>
          <Col xs={24} sm={12} md={8}><Form.Item label="远用瞳距" name="pdFar"><Input /></Form.Item></Col>
        </Row>

        <Row gutter={12}>
          <Col xs={24} sm={12} md={8}><Form.Item label="近用瞳距" name="pdNear"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={8}><Form.Item label="下加光" name="addPower"><Input /></Form.Item></Col>
          <Col xs={24} sm={12} md={8}><Form.Item label="验光师" name="optometristName"><Input placeholder="默认操作人" /></Form.Item></Col>
        </Row>

        <Form.Item label="检查日期" name="examDate">
          <DatePicker style={{ width: '100%' }} placeholder="选择检查日期" />
        </Form.Item>

        <Form.Item label="备注" name="remark">
          <Input.TextArea rows={2} placeholder="选填，记录眼部情况或其他补充信息..." />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default OptometryFormModal;
