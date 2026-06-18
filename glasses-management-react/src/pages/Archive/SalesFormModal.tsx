import React from 'react';
import { Modal, Form, Input, InputNumber, Row, Col, Divider, Select, message } from 'antd';
import { salesAPI, SalesRecord } from '@/api/sales';
import type { TimelineItem } from '@/api/archive';

export type SalesForm = Omit<Partial<SalesRecord>, 'id'> & { id?: number | null; customerId: number };

export const buildEmptySales = (customerId: number): SalesForm => ({
  id: null,
  customerId,
  optometryId: null,
  frameBrand: '',
  frameModel: '',
  frameQuantity: 1,
  frameRetailPrice: 0,
  framePrice: 0,
  lensBrand: '',
  lensParams: '',
  lensQuantity: 1,
  lensRetailPrice: 0,
  lensPrice: 0,
  totalRetailPrice: 0,
  totalAmount: 0,
  remark: '',
});

interface Props {
  open: boolean;
  initial: SalesForm | null;
  optoOptions: TimelineItem[];
  onClose: () => void;
  onSaved: () => void;
}

const SalesFormModal: React.FC<Props> = ({ open, initial, optoOptions, onClose, onSaved }) => {
  const [form] = Form.useForm<SalesForm>();
  const [submitting, setSubmitting] = React.useState(false);
  const [discount, setDiscount] = React.useState('-');

  React.useEffect(() => {
    if (open && initial) {
      form.setFieldsValue(initial);
      updateDiscount(initial.totalRetailPrice || 0, initial.totalAmount || 0);
    }
  }, [open, initial, form]);

  const updateDiscount = (retail: number, actual: number) => {
    if (retail <= 0) return setDiscount('-');
    setDiscount(((actual / retail) * 10).toFixed(1) + '折');
  };

  const calcTotal = () => {
    const fq = form.getFieldValue('frameQuantity') || 1;
    const lq = form.getFieldValue('lensQuantity') || 1;
    const fRetail = form.getFieldValue('frameRetailPrice') || 0;
    const lRetail = form.getFieldValue('lensRetailPrice') || 0;
    const fPrice = form.getFieldValue('framePrice') || 0;
    const lPrice = form.getFieldValue('lensPrice') || 0;
    const totalRetail = fRetail * fq + lRetail * lq;
    const total = fPrice * fq + lPrice * lq;
    form.setFieldsValue({ totalRetailPrice: totalRetail, totalAmount: total });
    updateDiscount(totalRetail, total);
  };

  const onTotalsChange = () => {
    const retail = form.getFieldValue('totalRetailPrice') || 0;
    const actual = form.getFieldValue('totalAmount') || 0;
    updateDiscount(retail, actual);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (values.id) {
        await salesAPI.update(values as any);
        message.success('更新成功');
      } else {
        await salesAPI.add(values as any);
        message.success('配镜开单成功');
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
  const optoChoices = optoOptions.filter((i) => i.type === 'OPTOMETRY');

  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={submitting}
      title={isEdit ? '编辑配镜单' : '新建配镜单'}
      okText="确认开单"
      cancelText="取消"
      width="min(94vw, 800px)"
      forceRender
    >
      <Form form={form} layout="horizontal" labelCol={{ flex: '88px' }} className="record-form" preserve={false}>
        <Form.Item name="id" hidden><Input /></Form.Item>
        <Form.Item name="customerId" hidden><Input /></Form.Item>

        <Form.Item label="关联验光" name="optometryId">
          <Select placeholder="选择验光记录(可选)" allowClear>
            {optoChoices.map((item) => (
              <Select.Option key={item.data.id} value={item.data.id}>
                {item.date} 验光单
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <h4 className="form-block-title" style={{ marginTop: 8 }}>镜架信息</h4>
        <Row gutter={12}>
          <Col xs={24} sm={12}><Form.Item label="镜架品牌" name="frameBrand"><Input /></Form.Item></Col>
          <Col xs={24} sm={12}><Form.Item label="镜架型号" name="frameModel"><Input /></Form.Item></Col>
        </Row>
        <Row gutter={12}>
          <Col xs={24} sm={8}><Form.Item label="数量" name="frameQuantity"><InputNumber min={1} step={1} onChange={calcTotal} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={24} sm={8}><Form.Item label="零售单价" name="frameRetailPrice"><InputNumber precision={2} step={10} onChange={calcTotal} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={24} sm={8}><Form.Item label="实际售价" name="framePrice"><InputNumber precision={2} step={10} onChange={calcTotal} style={{ width: '100%' }} /></Form.Item></Col>
        </Row>

        <Divider style={{ margin: '16px 0' }} />

        <h4 className="form-block-title">镜片信息</h4>
        <Row gutter={12}>
          <Col xs={24} sm={12}><Form.Item label="镜片品牌" name="lensBrand"><Input /></Form.Item></Col>
          <Col xs={24} sm={12}><Form.Item label="镜片参数" name="lensParams"><Input placeholder="例: 1.60 防蓝光" /></Form.Item></Col>
        </Row>
        <Row gutter={12}>
          <Col xs={24} sm={8}><Form.Item label="数量" name="lensQuantity"><InputNumber min={1} step={1} onChange={calcTotal} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={24} sm={8}><Form.Item label="零售单价" name="lensRetailPrice"><InputNumber precision={2} step={10} onChange={calcTotal} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={24} sm={8}><Form.Item label="实际售价" name="lensPrice"><InputNumber precision={2} step={10} onChange={calcTotal} style={{ width: '100%' }} /></Form.Item></Col>
        </Row>

        <Divider style={{ margin: '16px 0' }} />

        <h4 className="form-block-title">结算与备注</h4>
        <Row gutter={12}>
          <Col xs={24} sm={8}><Form.Item label="零售总价" name="totalRetailPrice"><InputNumber precision={2} step={10} onChange={onTotalsChange} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={24} sm={8}><Form.Item label="实收总价" name="totalAmount"><InputNumber precision={2} step={10} onChange={onTotalsChange} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={24} sm={8}><Form.Item label="折扣"><Input value={discount} readOnly placeholder="自动计算" /></Form.Item></Col>
        </Row>
        <Form.Item label="备注" name="remark">
          <Input.TextArea rows={2} placeholder="选填，如：送隐形眼镜盒..." />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default SalesFormModal;
