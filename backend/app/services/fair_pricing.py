"""Artisan-entered costs only. No invented wage or regional benchmark."""
from decimal import Decimal, ROUND_HALF_UP
from pydantic import BaseModel, Field, ConfigDict


class CostInputs(BaseModel):
    model_config = ConfigDict(extra='forbid', allow_inf_nan=False)
    hours: float = Field(ge=0, le=10000)
    hourly_rate: float = Field(gt=0, le=50000)
    material_cost: float = Field(ge=0, le=50000)
    margin_percent: float = Field(ge=0, le=200)


def calculate_price(costs: CostInputs) -> dict:
    def money(value):
        return float(value.quantize(Decimal('.01'), rounding=ROUND_HALF_UP))
    labor = Decimal(str(costs.hours)) * Decimal(str(costs.hourly_rate))
    material = Decimal(str(costs.material_cost))
    margin = (labor + material) * Decimal(str(costs.margin_percent)) / 100
    total = money(labor + material + margin)
    if total <= 0 or total > 50000:
        raise ValueError('Calculated price must be above zero and at most INR 50,000')
    return dict(source='artisan_cost_plus', suggested_price=total, recommended_price=total,
                low=total, high=total, suggested_price_low=total, suggested_price_high=total,
                labor_cost=money(labor), material_cost=money(material), margin=money(margin),
                calculation=f'{costs.hours} hours × INR {costs.hourly_rate} + INR {costs.material_cost} materials + {costs.margin_percent}% markup',
                explanation='Calculated from your entered costs. This is not a market valuation. You decide the selling price.',
                assumptions=['Hours, hourly rate and costs supplied by artisan.', 'Margin is markup on labor plus materials; taxes and delivery are excluded.', 'No regional benchmark is used.'],
                confidence='input_based', comparables=[])


def missing_costs():
    return dict(source='needs_artisan_input', suggested_price=0, low=None, high=None, comparables=[],
                explanation='Enter your work hours, hourly rate, material cost and margin to calculate a price. No price has been guessed.',
                required_fields=['hours', 'hourly_rate', 'material_cost', 'margin_percent'])
