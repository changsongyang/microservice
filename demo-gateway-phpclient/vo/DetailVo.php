<?php
namespace vo;

class DetailVo implements \JsonSerializable {
    private $detail_no;
    private $name;
    private $count;
    private $amount;

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
    public function getDetailNo()
    {
        return $this->detail_no;
    }

    /**
     * @param mixed $detail_no
     */
    public function setDetailNo($detail_no)
    {
        $this->detail_no = $detail_no;
    }

    /**
     * @return mixed
     */
    public function getName()
    {
        return $this->name;
    }

    /**
     * @param mixed $name
     */
    public function setName($name)
    {
        $this->name = $name;
    }

    /**
     * @return mixed
     */
    public function getCount()
    {
        return $this->count;
    }

    /**
     * @param mixed $count
     */
    public function setCount($count)
    {
        $this->count = $count;
    }

    /**
     * @return mixed
     */
    public function getAmount()
    {
        return $this->amount;
    }

    /**
     * @param mixed $amount
     */
    public function setAmount($amount)
    {
        $this->amount = $amount;
    }


}