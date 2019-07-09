<?php
namespace vo;

class BatchVo implements \JsonSerializable {
    private $batch_no;
    private $total_count;
    private $request_time;
    private $total_amount;
    private $details;

    /**
     * 需要实现此方法，以便json_encode()方法能返回私有属性
     * @return array|mixed
     */
    public function jsonSerialize(){
        $data = [];
        foreach ($this as $key => $val){
            $data[$key] = $val;
        }
        return $data;
    }

    /**
     * @return mixed
     */
    public function getBatchNo()
    {
        return $this->batch_no;
    }

    /**
     * @param mixed $batch_no
     */
    public function setBatchNo($batch_no)
    {
        $this->batch_no = $batch_no;
    }

    /**
     * @return mixed
     */
    public function getTotalCount()
    {
        return $this->total_count;
    }

    /**
     * @param mixed $total_count
     */
    public function setTotalCount($total_count)
    {
        $this->total_count = $total_count;
    }

    /**
     * @return mixed
     */
    public function getRequestTime()
    {
        return $this->request_time;
    }

    /**
     * @param mixed $request_time
     */
    public function setRequestTime($request_time)
    {
        $this->request_time = $request_time;
    }

    /**
     * @return mixed
     */
    public function getTotalAmount()
    {
        return $this->total_amount;
    }

    /**
     * @param mixed $total_amount
     */
    public function setTotalAmount($total_amount)
    {
        $this->total_amount = $total_amount;
    }

    /**
     * @return mixed
     */
    public function getDetails()
    {
        return $this->details;
    }

    /**
     * @param mixed $details
     */
    public function setDetails(array $details)
    {
        $this->details = $details;
    }


}